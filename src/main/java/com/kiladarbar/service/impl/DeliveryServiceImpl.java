package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.CompleteDeliveryRequest;
import com.kiladarbar.dto.response.DeliveryEarningsResponse;
import com.kiladarbar.dto.response.DeliveryOrderResponse;
import com.kiladarbar.dto.response.DeliveryPartnerProfileResponse;
import com.kiladarbar.event.OrderStatusChangedEvent;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.*;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.repository.DeliveryAssignmentRepository;
import com.kiladarbar.repository.DeliveryPartnerRepository;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.service.DeliveryService;
import com.kiladarbar.service.FcmService;
import com.kiladarbar.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryServiceImpl implements DeliveryService {

    private static final List<String> ACTIVE_STATUSES = List.of("ASSIGNED", "ACCEPTED", "PICKED_UP");
    private static final BigDecimal EARNING_PER_DELIVERY = BigDecimal.valueOf(40);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryAssignmentRepository assignmentRepository;
    private final OrderRepository orderRepository;
    private final LoyaltyService loyaltyService;
    private final ApplicationEventPublisher eventPublisher;
    private final FcmService fcmService;

    // ─────────────────────────── Assignment ───────────────────────────

    @Override
    public void autoAssignDriver(Order order) {
        if (order.getBranch() == null) return;
        List<DeliveryPartner> available = deliveryPartnerRepository
                .findByBranchIdAndAvailableTrue(order.getBranch().getId());
        if (available.isEmpty()) {
            // fall back to any available partner regardless of branch
            available = deliveryPartnerRepository.findByAvailableTrue();
        }
        if (available.isEmpty()) {
            log.warn("No available drivers for order: {}", order.getOrderNumber());
            return;
        }
        // Spread load: fewest lifetime deliveries first
        DeliveryPartner partner = available.stream()
                .min(Comparator.comparingInt(DeliveryPartner::getTotalDeliveries))
                .get();
        createOrReassign(order, partner);
    }

    @Override
    public void assignSpecificDriver(Order order, UUID driverId) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found"));
        createOrReassign(order, partner);
    }

    /** One assignment row per order — reassignment reuses the row. */
    private void createOrReassign(Order order, DeliveryPartner partner) {
        DeliveryAssignment assignment = assignmentRepository.findByOrderId(order.getId())
                .orElseGet(() -> DeliveryAssignment.builder().order(order).build());

        if ("DELIVERED".equals(assignment.getStatus())) {
            throw new BusinessException("Order is already delivered");
        }

        assignment.setPartner(partner);
        assignment.setStatus("ASSIGNED");
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setAcceptedAt(null);
        assignment.setPickedUpAt(null);
        if (assignment.getDeliveryOtp() == null) {
            assignment.setDeliveryOtp(String.format("%04d", RANDOM.nextInt(10_000)));
        }
        assignmentRepository.save(assignment);
        log.info("Assigned driver {} to order {}", partner.getId(), order.getOrderNumber());
        notifyDriverAssigned(partner, order, assignment);
    }

    /** Data-only FCM push so the driver app surfaces (and refreshes on) a new assignment. */
    private void notifyDriverAssigned(DeliveryPartner partner, Order order, DeliveryAssignment assignment) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "delivery_assignment");
        data.put("assignment_id", assignment.getId().toString());
        data.put("order_id", order.getId().toString());
        data.put("order_number", order.getOrderNumber());
        data.put("title", "New Delivery Assigned 🛵");
        data.put("body", "Order #" + order.getOrderNumber() + " — tap to view and accept.");
        fcmService.sendToUser(partner.getId(), data);
    }

    // ─────────────────────────── Driver profile ───────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DeliveryPartnerProfileResponse getProfile(UUID partnerId) {
        DeliveryPartner partner = findPartner(partnerId);
        User user = partner.getUser();
        return DeliveryPartnerProfileResponse.builder()
                .id(partner.getId())
                .name(user != null ? user.getName() : null)
                .phone(user != null ? user.getPhone() : null)
                .email(user != null ? user.getEmail() : null)
                .vehicleType(partner.getVehicleType())
                .vehicleNumber(partner.getVehicleNumber())
                .branchName(partner.getBranch() != null ? partner.getBranch().getName() : null)
                .available(partner.isAvailable())
                .rating(partner.getRating())
                .totalDeliveries(partner.getTotalDeliveries())
                .build();
    }

    @Override
    public void setAvailability(UUID partnerId, boolean available) {
        DeliveryPartner partner = findPartner(partnerId);
        partner.setAvailable(available);
        deliveryPartnerRepository.save(partner);
    }

    @Override
    public void updateLocation(UUID partnerId, BigDecimal lat, BigDecimal lng) {
        DeliveryPartner partner = findPartner(partnerId);
        partner.setCurrentLat(lat);
        partner.setCurrentLng(lng);
        partner.setLastLocationAt(LocalDateTime.now());
        deliveryPartnerRepository.save(partner);
    }

    // ─────────────────────────── Driver orders ───────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> getActiveOrders(UUID partnerId) {
        return assignmentRepository
                .findByPartnerIdAndStatusInOrderByAssignedAtDesc(partnerId, ACTIVE_STATUSES)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DeliveryOrderResponse> getDeliveryHistory(UUID partnerId, Pageable pageable) {
        return assignmentRepository
                .findByPartnerIdAndStatusOrderByDeliveredAtDesc(partnerId, "DELIVERED", pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOrderResponse getAssignment(UUID assignmentId, UUID partnerId) {
        return mapToResponse(findAssignment(assignmentId, partnerId));
    }

    @Override
    public DeliveryOrderResponse acceptAssignment(UUID assignmentId, UUID partnerId) {
        DeliveryAssignment assignment = findAssignment(assignmentId, partnerId);
        requireStatus(assignment, "ASSIGNED", "accept");
        assignment.setStatus("ACCEPTED");
        assignment.setAcceptedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);
        return mapToResponse(assignment);
    }

    @Override
    public DeliveryOrderResponse rejectAssignment(UUID assignmentId, UUID partnerId) {
        DeliveryAssignment assignment = findAssignment(assignmentId, partnerId);
        requireStatus(assignment, "ASSIGNED", "reject");

        // Try to hand the order to someone else; otherwise leave it for admin dispatch
        Order order = assignment.getOrder();
        List<DeliveryPartner> others = deliveryPartnerRepository.findByAvailableTrue().stream()
                .filter(p -> !p.getId().equals(partnerId))
                .collect(Collectors.toList());
        if (!others.isEmpty()) {
            DeliveryPartner next = others.stream()
                    .min(Comparator.comparingInt(DeliveryPartner::getTotalDeliveries)).get();
            createOrReassign(order, next);
        } else {
            assignment.setStatus("REJECTED");
            assignmentRepository.save(assignment);
            log.warn("Order {} rejected by {} and no other drivers available",
                    order.getOrderNumber(), partnerId);
        }
        return mapToResponse(assignment);
    }

    @Override
    public DeliveryOrderResponse markPickedUp(UUID assignmentId, UUID partnerId) {
        DeliveryAssignment assignment = findAssignment(assignmentId, partnerId);
        requireStatus(assignment, "ACCEPTED", "pick up");

        assignment.setStatus("PICKED_UP");
        assignment.setPickedUpAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        Order order = assignment.getOrder();
        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, previous, OrderStatus.OUT_FOR_DELIVERY));

        return mapToResponse(assignment);
    }

    @Override
    public DeliveryOrderResponse completeDelivery(UUID assignmentId, UUID partnerId,
                                                  CompleteDeliveryRequest request) {
        DeliveryAssignment assignment = findAssignment(assignmentId, partnerId);
        requireStatus(assignment, "PICKED_UP", "complete");

        if (assignment.getDeliveryOtp() != null && request.getOtp() != null
                && !assignment.getDeliveryOtp().equals(request.getOtp().trim())) {
            throw new BusinessException("Incorrect delivery OTP");
        }

        Order order = assignment.getOrder();

        // COD: cash must be collected before completing
        Payment payment = order.getPayment();
        if (payment != null && "COD".equals(payment.getMethod())) {
            if (!request.isCashCollected()) {
                throw new BusinessException("Confirm cash collection to complete a COD delivery");
            }
            payment.setStatus("SUCCESS");
        }

        LocalDateTime now = LocalDateTime.now();
        assignment.setStatus("DELIVERED");
        assignment.setDeliveredAt(now);
        assignment.setEarnings(EARNING_PER_DELIVERY);
        if (assignment.getPickedUpAt() != null) {
            assignment.setDeliveryDurationMin(
                    (short) java.time.Duration.between(assignment.getPickedUpAt(), now).toMinutes());
        }
        assignmentRepository.save(assignment);

        DeliveryPartner partner = assignment.getPartner();
        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        deliveryPartnerRepository.save(partner);

        OrderStatus previous = order.getStatus();
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(now);
        earnLoyaltyPoints(order);
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderStatusChangedEvent(order, previous, OrderStatus.DELIVERED));

        log.info("Order {} delivered by {} — COD collected: {}",
                order.getOrderNumber(), partnerId, request.isCashCollected());
        return mapToResponse(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryEarningsResponse getEarnings(UUID partnerId) {
        DeliveryPartner partner = findPartner(partnerId);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek = LocalDate.now().minusDays(6).atStartOfDay();
        return DeliveryEarningsResponse.builder()
                .today(assignmentRepository.sumEarningsSince(partnerId, startOfDay))
                .thisWeek(assignmentRepository.sumEarningsSince(partnerId, startOfWeek))
                .total(assignmentRepository.sumTotalEarnings(partnerId))
                .deliveriesToday(assignmentRepository.countDeliveredSince(partnerId, startOfDay))
                .deliveriesThisWeek(assignmentRepository.countDeliveredSince(partnerId, startOfWeek))
                .totalDeliveries(partner.getTotalDeliveries())
                .build();
    }

    // ─────────────────────────── Helpers ───────────────────────────

    private DeliveryPartner findPartner(UUID partnerId) {
        return deliveryPartnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You are not registered as a delivery partner"));
    }

    private DeliveryAssignment findAssignment(UUID assignmentId, UUID partnerId) {
        return assignmentRepository.findByIdAndPartnerId(assignmentId, partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery assignment not found"));
    }

    private void requireStatus(DeliveryAssignment assignment, String expected, String action) {
        if (!expected.equals(assignment.getStatus())) {
            throw new BusinessException("Cannot " + action + " a delivery in status " + assignment.getStatus());
        }
    }

    private void earnLoyaltyPoints(Order order) {
        if (order.getUser() != null) {
            int points = order.getTotalAmount().multiply(BigDecimal.valueOf(0.1)).intValue();
            order.setPointsEarned(points);
            loyaltyService.earnPoints(order.getUser().getId(), points, order.getId());
        }
    }

    private DeliveryOrderResponse mapToResponse(DeliveryAssignment assignment) {
        Order order = assignment.getOrder();
        User customer = order.getUser();
        Branch branch = order.getBranch();
        UserAddress address = order.getDeliveryAddress();
        Payment payment = order.getPayment();

        boolean cod = payment != null && "COD".equals(payment.getMethod())
                && !"SUCCESS".equals(payment.getStatus());

        return DeliveryOrderResponse.builder()
                .assignmentId(assignment.getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(assignment.getStatus())
                .orderStatus(order.getStatus().name())
                .paymentMethod(payment != null ? payment.getMethod() : "COD")
                .amountToCollect(cod ? order.getTotalAmount() : BigDecimal.ZERO)
                .customerName(customer != null ? customer.getName() : null)
                .customerPhone(customer != null ? customer.getPhone() : null)
                .deliveryAddress(formatAddress(address))
                .deliveryLat(order.getDeliveryLat())
                .deliveryLng(order.getDeliveryLng())
                .deliveryInstructions(order.getDeliveryInstructions())
                .branchName(branch != null ? branch.getName() : null)
                .branchAddress(branch != null ? branch.getAddressLine1() + ", " + branch.getCity() : null)
                .branchPhone(branch != null ? branch.getPhone() : null)
                .items(order.getItems().stream()
                        .map(i -> DeliveryOrderResponse.ItemLine.builder()
                                .name(i.getName())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .itemCount(order.getItems().stream().mapToInt(OrderItem::getQuantity).sum())
                .earnings(assignment.getEarnings())
                .assignedAt(assignment.getAssignedAt())
                .acceptedAt(assignment.getAcceptedAt())
                .pickedUpAt(assignment.getPickedUpAt())
                .deliveredAt(assignment.getDeliveredAt())
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
}
