package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CreateOrderRequest;
import com.kiladarbar.dto.request.RateOrderRequest;
import com.kiladarbar.dto.response.OrderResponse;
import com.kiladarbar.event.OrderStatusChangedEvent;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.*;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import com.kiladarbar.repository.*;
import com.kiladarbar.service.DeliveryService;
import com.kiladarbar.service.InventoryService;
import com.kiladarbar.service.LoyaltyService;
import com.kiladarbar.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final BranchRepository branchRepository;
    private final CouponRepository couponRepository;
    private final LoyaltyService loyaltyService;
    private final DeliveryService deliveryService;
    private final InventoryService inventoryService;
    private final ApplicationEventPublisher eventPublisher;

    private static final Set<String> ALLOWED_PAYMENT_METHODS = Set.of("COD", "CASH_ON_DELIVERY");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = BigDecimal.valueOf(500);
    private static final BigDecimal DELIVERY_BASE = BigDecimal.valueOf(40);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public OrderResponse placeOrder(CreateOrderRequest request, UUID userId) {
        validatePaymentMethod(request.getPaymentMethod());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new BusinessException("Branch is currently closed");
        }

        Order order = buildOrder(request, user, branch);
        buildOrderItems(order, request.getItems());
        applyDeliveryAddress(order, request, user);

        // Apply coupon if provided
        if (request.getCouponCode() != null) {
            applyCoupon(order, request.getCouponCode(), user);
        }

        // Apply loyalty points redemption
        if (request.getRedeemPoints() != null && request.getRedeemPoints() > 0) {
            applyLoyaltyRedemption(order, user, request.getRedeemPoints());
        }

        // Calculate final totals
        calculateTotals(order);

        attachCodPayment(order, user);

        // COD orders need no payment gateway hand-off — confirm immediately
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Deduct inventory
        inventoryService.deductForOrder(savedOrder);

        // Trigger auto-assignment for delivery orders
        if (order.getOrderType() == OrderType.DELIVERY) {
            deliveryService.autoAssignDriver(savedOrder);
        }

        // Publish event for notifications, KDS, etc.
        eventPublisher.publishEvent(new OrderStatusChangedEvent(savedOrder, null, OrderStatus.CONFIRMED));

        log.info("Order placed (COD): {} for user {}", savedOrder.getOrderNumber(), userId);
        return mapToResponse(savedOrder);
    }

    private void validatePaymentMethod(String method) {
        if (method != null && !ALLOWED_PAYMENT_METHODS.contains(method.toUpperCase())) {
            throw new BusinessException("Only Cash on Delivery is available as a payment method");
        }
    }

    private void buildOrderItems(Order order, List<CreateOrderRequest.OrderItemRequest> itemRequests) {
        for (CreateOrderRequest.OrderItemRequest itemReq : itemRequests) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new BusinessException(menuItem.getName() + " is currently unavailable");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() < 1) {
                throw new BusinessException("Invalid quantity for " + menuItem.getName());
            }

            BigDecimal unitPrice = menuItem.getDiscountPrice() != null
                    ? menuItem.getDiscountPrice() : menuItem.getPrice();

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .name(menuItem.getName())
                    .quantity(itemReq.getQuantity().shortValue())
                    .unitPrice(unitPrice)
                    .gstRate(menuItem.getGstRate())
                    .totalPrice(unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity())))
                    .specialInstruction(itemReq.getSpecialInstruction())
                    .build();
            order.getItems().add(item);
        }
    }

    private void applyDeliveryAddress(Order order, CreateOrderRequest request, User user) {
        if (order.getOrderType() != OrderType.DELIVERY) return;

        if (request.getDeliveryAddressId() == null) {
            throw new BusinessException("Delivery address is required for delivery orders");
        }
        UserAddress address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(request.getDeliveryAddressId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Delivery address not found"));

        order.setDeliveryAddress(address);
        order.setDeliveryLat(address.getLatitude());
        order.setDeliveryLng(address.getLongitude());
    }

    private void attachCodPayment(Order order, User user) {
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(order.getTotalAmount())
                .currency("INR")
                .method("COD")
                .gateway("INTERNAL")
                .status("PENDING")   // becomes SUCCESS when driver collects cash
                .build();
        order.setPayment(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId, String reason, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        validateCancellation(order);

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancellationReason(reason);

        Order saved = orderRepository.save(order);

        // Reverse inventory deduction
        inventoryService.reverseOrderDeduction(saved);

        // Refund points if redeemed
        if (order.getPointsRedeemed() > 0) {
            loyaltyService.refundPoints(userId, order.getPointsRedeemed(), orderId);
        }

        eventPublisher.publishEvent(new OrderStatusChangedEvent(saved, previousStatus, OrderStatus.CANCELLED));

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);

        // Set timestamps for status transitions
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case PREPARING -> order.setPreparingAt(LocalDateTime.now());
            case READY -> order.setReadyAt(LocalDateTime.now());
            case DELIVERED -> {
                order.setDeliveredAt(LocalDateTime.now());
                earnLoyaltyPoints(order);
            }
        }

        Order saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(saved, previousStatus, newStatus));

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse assignDriver(UUID orderId, UUID driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        deliveryService.assignSpecificDriver(order, driverId);
        return mapToResponse(orderRepository.findById(orderId).get());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrder(UUID orderId, UUID userId) {
        return getOrderById(orderId, userId);
    }

    @Override
    public void rateOrder(UUID orderId, RateOrderRequest request, UUID userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Order must be delivered before rating");
        }
        // Create review logic here
    }

    @Override
    public OrderResponse reorder(UUID orderId, UUID userId) {
        Order originalOrder = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        CreateOrderRequest reorderRequest = buildReorderRequest(originalOrder);
        return placeOrder(reorderRequest, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> adminListOrders(UUID branchId, OrderStatus status, OrderType type,
                                               LocalDate from, LocalDate to, Pageable pageable) {
        return orderRepository.findWithFilters(branchId, status, type, from, to, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse adminGetOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    @Override
    public void refundOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);
        // Trigger payment refund
    }

    private Order buildOrder(CreateOrderRequest request, User user, Branch branch) {
        return Order.builder()
                .orderNumber(generateOrderNumber())
                .branch(branch)
                .user(user)
                .orderType(request.getOrderType())
                .status(OrderStatus.PENDING)
                .deliveryInstructions(request.getDeliveryInstructions())
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .deliveryCharge(BigDecimal.ZERO)
                .packagingCharge(BigDecimal.ZERO)
                .cgstAmount(BigDecimal.ZERO)
                .sgstAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .tipAmount(request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO)
                .scheduled(request.isScheduled())
                .scheduledAt(request.getScheduledAt())
                .build();
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // Random suffix survives restarts; the unique index on order_number guards collisions
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = String.format("KD-%s-%05d", date, RANDOM.nextInt(100_000));
            if (!orderRepository.existsByOrderNumber(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException("Could not generate order number, please retry");
    }

    private void applyCoupon(Order order, String couponCode, User user) {
        couponRepository.findActiveByCode(couponCode)
                .ifPresent(coupon -> {
                    BigDecimal discount = calculateCouponDiscount(coupon, order);
                    order.setDiscountAmount(discount);
                    order.setCouponCode(couponCode);
                });
    }

    private BigDecimal calculateCouponDiscount(Coupon coupon, Order order) {
        return switch (coupon.getType()) {
            case "PERCENT" -> order.getSubtotal()
                    .multiply(coupon.getValue())
                    .divide(BigDecimal.valueOf(100));
            case "FLAT" -> coupon.getValue().min(order.getSubtotal());
            default -> BigDecimal.ZERO;
        };
    }

    private void applyLoyaltyRedemption(Order order, User user, int points) {
        int availablePoints = loyaltyService.getAvailablePoints(user.getId());
        int pointsToRedeem = Math.min(points, availablePoints);
        BigDecimal discount = loyaltyService.pointsToRupees(pointsToRedeem);
        order.setPointsRedeemed(pointsToRedeem);
        order.setDiscountAmount(order.getDiscountAmount().add(discount));
    }

    private void calculateTotals(Order order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        if (order.getOrderType() == OrderType.DELIVERY) {
            order.setDeliveryCharge(subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0
                    ? BigDecimal.ZERO : DELIVERY_BASE);
        }

        BigDecimal taxable = subtotal.subtract(order.getDiscountAmount());
        BigDecimal gstRate = new BigDecimal("0.05");
        BigDecimal halfGst = taxable.multiply(gstRate)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        order.setCgstAmount(halfGst);
        order.setSgstAmount(halfGst);

        BigDecimal total = taxable
                .add(order.getCgstAmount())
                .add(order.getSgstAmount())
                .add(order.getDeliveryCharge())
                .add(order.getPackagingCharge())
                .add(order.getTipAmount());
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
    }

    private void earnLoyaltyPoints(Order order) {
        if (order.getUser() != null) {
            int points = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(0.1))
                    .intValue();
            order.setPointsEarned(points);
            loyaltyService.earnPoints(order.getUser().getId(), points, order.getId());
        }
    }

    private void validateCancellation(Order order) {
        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED ||
            order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
            throw new BusinessException("Order cannot be cancelled in its current state: " + order.getStatus());
        }
    }

    private CreateOrderRequest buildReorderRequest(Order originalOrder) {
        List<CreateOrderRequest.OrderItemRequest> items = originalOrder.getItems().stream()
                .map(item -> {
                    CreateOrderRequest.OrderItemRequest req = new CreateOrderRequest.OrderItemRequest();
                    req.setMenuItemId(item.getMenuItem().getId());
                    req.setQuantity((int) item.getQuantity());
                    req.setSpecialInstruction(item.getSpecialInstruction());
                    return req;
                })
                .collect(Collectors.toList());

        return CreateOrderRequest.builder()
                .branchId(originalOrder.getBranch().getId())
                .orderType(originalOrder.getOrderType())
                .items(items)
                .deliveryAddressId(originalOrder.getDeliveryAddress() != null
                        ? originalOrder.getDeliveryAddress().getId() : null)
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .branchName(order.getBranch() != null ? order.getBranch().getName() : null)
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .packagingCharge(order.getPackagingCharge())
                .tipAmount(order.getTipAmount())
                .cgstAmount(order.getCgstAmount())
                .sgstAmount(order.getSgstAmount())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(item -> OrderResponse.OrderItemResponse.builder()
                                .id(item.getId())
                                .name(item.getName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalPrice(item.getTotalPrice())
                                .kdsStatus(item.getKdsStatus())
                                .build())
                        .collect(Collectors.toList()))
                .paymentMethod(order.getPayment() != null ? order.getPayment().getMethod() : null)
                .paymentStatus(order.getPayment() != null ? order.getPayment().getStatus() : null)
                .couponCode(order.getCouponCode())
                .deliveryInfo(buildDeliveryInfo(order))
                .deliveryAddress(formatAddress(order.getDeliveryAddress()))
                .estimatedMinutes(estimateMinutes(order))
                .pointsEarned(order.getPointsEarned())
                .pointsRedeemed(order.getPointsRedeemed())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .deliveredAt(order.getDeliveredAt())
                .cancelledAt(order.getCancelledAt())
                .cancellationReason(order.getCancellationReason())
                .build();
    }

    private OrderResponse.DeliveryInfo buildDeliveryInfo(Order order) {
        DeliveryAssignment assignment = order.getDeliveryAssignment();
        if (assignment == null || assignment.getPartner() == null) return null;
        DeliveryPartner partner = assignment.getPartner();
        User driverUser = partner.getUser();
        return OrderResponse.DeliveryInfo.builder()
                .driverName(driverUser != null ? driverUser.getName() : null)
                .driverPhone(driverUser != null ? driverUser.getPhone() : null)
                .driverLat(partner.getCurrentLat())
                .driverLng(partner.getCurrentLng())
                .estimatedMinutes(estimateMinutes(order))
                .deliveryOtp(assignment.getDeliveryOtp())
                .build();
    }

    private String formatAddress(UserAddress address) {
        if (address == null) return null;
        StringBuilder sb = new StringBuilder(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        if (address.getLandmark() != null && !address.getLandmark().isBlank()) {
            sb.append(", near ").append(address.getLandmark());
        }
        sb.append(", ").append(address.getCity()).append(" - ").append(address.getPincode());
        return sb.toString();
    }

    private Integer estimateMinutes(Order order) {
        return switch (order.getStatus()) {
            case CONFIRMED, PENDING -> 40;
            case PREPARING -> 30;
            case READY -> 20;
            case OUT_FOR_DELIVERY -> 15;
            default -> null;
        };
    }
}
