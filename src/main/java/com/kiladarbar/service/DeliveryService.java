package com.kiladarbar.service;

import com.kiladarbar.dto.request.CompleteDeliveryRequest;
import com.kiladarbar.dto.response.DeliveryEarningsResponse;
import com.kiladarbar.dto.response.DeliveryOrderResponse;
import com.kiladarbar.dto.response.DeliveryPartnerProfileResponse;
import com.kiladarbar.model.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DeliveryService {

    // ── Assignment (called from order flow / admin) ──
    void autoAssignDriver(Order order);
    void assignSpecificDriver(Order order, UUID driverId);

    // ── Driver-facing (partnerId = authenticated driver's user id) ──
    DeliveryPartnerProfileResponse getProfile(UUID partnerId);
    void setAvailability(UUID partnerId, boolean available);
    void updateLocation(UUID partnerId, BigDecimal lat, BigDecimal lng);

    List<DeliveryOrderResponse> getActiveOrders(UUID partnerId);
    Page<DeliveryOrderResponse> getDeliveryHistory(UUID partnerId, Pageable pageable);
    DeliveryOrderResponse getAssignment(UUID assignmentId, UUID partnerId);

    DeliveryOrderResponse acceptAssignment(UUID assignmentId, UUID partnerId);
    DeliveryOrderResponse rejectAssignment(UUID assignmentId, UUID partnerId);
    DeliveryOrderResponse markPickedUp(UUID assignmentId, UUID partnerId);
    DeliveryOrderResponse completeDelivery(UUID assignmentId, UUID partnerId, CompleteDeliveryRequest request);

    DeliveryEarningsResponse getEarnings(UUID partnerId);
}
