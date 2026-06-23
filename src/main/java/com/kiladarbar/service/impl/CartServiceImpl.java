package com.kiladarbar.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiladarbar.dto.request.AddCartItemRequest;
import com.kiladarbar.dto.request.UpdateCartItemRequest;
import com.kiladarbar.dto.response.CartResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.Coupon;
import com.kiladarbar.model.entity.MenuItem;
import com.kiladarbar.repository.CouponRepository;
import com.kiladarbar.repository.MenuItemRepository;
import com.kiladarbar.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MenuItemRepository menuItemRepository;
    private final CouponRepository couponRepository;
    private final ObjectMapper objectMapper;

    private static final String CART_PREFIX   = "cart:";
    private static final long CART_TTL_HOURS  = 48;
    private static final BigDecimal GST_RATE  = BigDecimal.valueOf(0.05);
    private static final BigDecimal DELIVERY_BASE = BigDecimal.valueOf(40);
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);

    @Override
    public CartResponse getCart(UUID userId) {
        return buildResponse(userId, loadItems(userId), loadCouponCode(userId));
    }

    @Override
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.isAvailable()) {
            throw new BusinessException("Item is currently unavailable");
        }

        Map<UUID, CartResponse.CartItem> items = loadItems(userId);
        CartResponse.CartItem existing = items.get(request.getMenuItemId());

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            items.put(request.getMenuItemId(), CartResponse.CartItem.builder()
                    .menuItemId(request.getMenuItemId())
                    .name(menuItem.getName())
                    .imageUrl(menuItem.getImages() != null && !menuItem.getImages().isEmpty()
                            ? menuItem.getImages().get(0).getUrl() : null)
                    .unitPrice(menuItem.getDiscountPrice() != null
                            ? menuItem.getDiscountPrice() : menuItem.getPrice())
                    .quantity(request.getQuantity())
                    .specialInstruction(request.getSpecialInstruction())
                    .build());
        }

        saveItems(userId, items);
        return buildResponse(userId, items, loadCouponCode(userId));
    }

    @Override
    public CartResponse updateItem(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        Map<UUID, CartResponse.CartItem> items = loadItems(userId);

        if (!items.containsKey(itemId)) {
            throw new ResourceNotFoundException("Item not in cart");
        }

        if (request.getQuantity() <= 0) {
            items.remove(itemId);
        } else {
            items.get(itemId).setQuantity(request.getQuantity());
        }

        saveItems(userId, items);
        return buildResponse(userId, items, loadCouponCode(userId));
    }

    @Override
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Map<UUID, CartResponse.CartItem> items = loadItems(userId);
        items.remove(itemId);
        saveItems(userId, items);
        return buildResponse(userId, items, loadCouponCode(userId));
    }

    @Override
    public void clearCart(UUID userId) {
        redisTemplate.delete(CART_PREFIX + userId);
        redisTemplate.delete(CART_PREFIX + userId + ":coupon");
    }

    @Override
    public CartResponse applyCoupon(UUID userId, String couponCode) {
        Coupon coupon = couponRepository.findActiveByCode(couponCode)
                .orElseThrow(() -> new BusinessException("Invalid or expired coupon"));

        if (coupon.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Coupon has expired");
        }

        redisTemplate.opsForValue().set(
                CART_PREFIX + userId + ":coupon", couponCode, CART_TTL_HOURS, TimeUnit.HOURS);
        return buildResponse(userId, loadItems(userId), couponCode);
    }

    @Override
    public CartResponse removeCoupon(UUID userId) {
        redisTemplate.delete(CART_PREFIX + userId + ":coupon");
        return buildResponse(userId, loadItems(userId), null);
    }

    /* ── Private helpers ── */

    private Map<UUID, CartResponse.CartItem> loadItems(UUID userId) {
        Object raw = redisTemplate.opsForValue().get(CART_PREFIX + userId);
        if (raw == null) return new LinkedHashMap<>();
        try {
            return objectMapper.convertValue(raw,
                    new TypeReference<LinkedHashMap<UUID, CartResponse.CartItem>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize cart for user {}: {}", userId, e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private void saveItems(UUID userId, Map<UUID, CartResponse.CartItem> items) {
        redisTemplate.opsForValue().set(CART_PREFIX + userId, items, CART_TTL_HOURS, TimeUnit.HOURS);
    }

    private String loadCouponCode(UUID userId) {
        Object raw = redisTemplate.opsForValue().get(CART_PREFIX + userId + ":coupon");
        return raw != null ? raw.toString() : null;
    }

    private CartResponse buildResponse(UUID userId, Map<UUID, CartResponse.CartItem> items, String couponCode) {
        List<CartResponse.CartItem> itemList = new ArrayList<>(items.values());

        // Compute each item total
        itemList.forEach(i -> i.setTotalPrice(
                i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()))));

        BigDecimal subtotal = itemList.stream()
                .map(CartResponse.CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        if (couponCode != null) {
            discount = computeCouponDiscount(couponCode, subtotal);
        }

        BigDecimal taxableAmount = subtotal.subtract(discount);
        BigDecimal gst = taxableAmount.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal delivery = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
                ? BigDecimal.ZERO : DELIVERY_BASE;
        BigDecimal total = taxableAmount.add(gst).add(delivery);

        return CartResponse.builder()
                .userId(userId)
                .items(itemList)
                .itemCount(itemList.stream().mapToInt(CartResponse.CartItem::getQuantity).sum())
                .subtotal(subtotal.setScale(2, RoundingMode.HALF_UP))
                .discountAmount(discount.setScale(2, RoundingMode.HALF_UP))
                .gstAmount(gst)
                .deliveryCharge(delivery)
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .appliedCoupon(couponCode)
                .freeDeliveryAbove(FREE_DELIVERY_THRESHOLD)
                .build();
    }

    private BigDecimal computeCouponDiscount(String code, BigDecimal subtotal) {
        return couponRepository.findActiveByCode(code)
                .filter(c -> subtotal.compareTo(c.getMinOrderAmount()) >= 0)
                .map(c -> {
                    BigDecimal disc = "PERCENTAGE".equals(c.getType())
                            ? subtotal.multiply(c.getValue().divide(BigDecimal.valueOf(100)))
                            : c.getValue();
                    if (c.getMaxDiscount() != null && disc.compareTo(c.getMaxDiscount()) > 0) {
                        disc = c.getMaxDiscount();
                    }
                    return disc;
                })
                .orElse(BigDecimal.ZERO);
    }
}
