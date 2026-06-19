package com.kiladarbar.service;

import com.kiladarbar.dto.request.CreateOrderRequest;
import com.kiladarbar.dto.request.RateOrderRequest;
import com.kiladarbar.dto.response.OrderResponse;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.UUID;

public interface OrderService {
    OrderResponse placeOrder(CreateOrderRequest request, UUID userId);
    OrderResponse getOrderById(UUID orderId, UUID userId);
    Page<OrderResponse> getUserOrders(UUID userId, Pageable pageable);
    OrderResponse cancelOrder(UUID orderId, String reason, UUID userId);
    OrderResponse updateOrderStatus(UUID orderId, OrderStatus status);
    OrderResponse assignDriver(UUID orderId, UUID driverId);
    OrderResponse trackOrder(UUID orderId, UUID userId);
    void rateOrder(UUID orderId, RateOrderRequest request, UUID userId);
    OrderResponse reorder(UUID orderId, UUID userId);
    Page<OrderResponse> adminListOrders(UUID branchId, OrderStatus status, OrderType type,
                                        LocalDate from, LocalDate to, Pageable pageable);
    OrderResponse adminGetOrder(UUID orderId);
    void refundOrder(UUID orderId, String reason);
}
