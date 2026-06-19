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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

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

    private final AtomicLong orderCounter = new AtomicLong(0);

    @Override
    public OrderResponse placeOrder(CreateOrderRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new BusinessException("Branch is currently closed");
        }

        Order order = buildOrder(request, user, branch);

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

        Order savedOrder = orderRepository.save(order);

        // Deduct inventory
        inventoryService.deductForOrder(savedOrder);

        // Trigger auto-assignment for delivery orders
        if (order.getOrderType() == OrderType.DELIVERY) {
            deliveryService.autoAssignDriver(savedOrder);
        }

        // Publish event for notifications, KDS, etc.
        eventPublisher.publishEvent(new OrderStatusChangedEvent(savedOrder, null, OrderStatus.PENDING));

        log.info("Order placed: {} for user {}", savedOrder.getOrderNumber(), userId);
        return mapToResponse(savedOrder);
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
                .tipAmount(request.getTipAmount() != null ? request.getTipAmount() : BigDecimal.ZERO)
                .scheduled(request.isScheduled())
                .scheduledAt(request.getScheduledAt())
                .build();
    }

    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = orderCounter.incrementAndGet();
        return String.format("KD-%s-%05d", date, seq);
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

        BigDecimal taxable = subtotal.subtract(order.getDiscountAmount());
        BigDecimal gstRate = new BigDecimal("0.05");
        BigDecimal halfGst = taxable.multiply(gstRate).divide(BigDecimal.valueOf(2));
        order.setCgstAmount(halfGst);
        order.setSgstAmount(halfGst);

        BigDecimal total = taxable
                .add(order.getCgstAmount())
                .add(order.getSgstAmount())
                .add(order.getDeliveryCharge())
                .add(order.getPackagingCharge())
                .add(order.getTipAmount());
        order.setTotalAmount(total);
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
        return CreateOrderRequest.builder()
                .branchId(originalOrder.getBranch().getId())
                .orderType(originalOrder.getOrderType())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .orderType(order.getOrderType())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .cgstAmount(order.getCgstAmount())
                .sgstAmount(order.getSgstAmount())
                .totalAmount(order.getTotalAmount())
                .pointsEarned(order.getPointsEarned())
                .pointsRedeemed(order.getPointsRedeemed())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .preparingAt(order.getPreparingAt())
                .readyAt(order.getReadyAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }
}
