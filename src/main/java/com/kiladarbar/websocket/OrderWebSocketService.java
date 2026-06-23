package com.kiladarbar.websocket;

import com.kiladarbar.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        var order = event.getOrder();

        OrderUpdatePayload payload = OrderUpdatePayload.builder()
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .status(event.getNewStatus().name())
                .previousStatus(event.getPreviousStatus() != null ? event.getPreviousStatus().name() : null)
                .estimatedMinutes(null)
                .message(statusMessage(event.getNewStatus().name()))
                .timestamp(LocalDateTime.now().toString())
                .build();

        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), payload);

        if (order.getBranch() != null) {
            messagingTemplate.convertAndSend("/topic/kitchen/" + order.getBranch().getId(), payload);
        }

        if (order.getUser() != null) {
            messagingTemplate.convertAndSendToUser(
                    order.getUser().getId().toString(), "/queue/orders", payload);
        }

        log.debug("WS event: order={} status={}", order.getId(), event.getNewStatus());
    }

    public void broadcastTableBooked(String branchId, Map<String, Object> data) {
        messagingTemplate.convertAndSend("/topic/reservations/" + branchId, data);
    }

    public void broadcastPaymentSuccess(String userId, Map<String, Object> data) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/payments", data);
    }

    private String statusMessage(String status) {
        return switch (status) {
            case "CONFIRMED"        -> "Your order has been confirmed!";
            case "PREPARING"        -> "Our chefs are preparing your royal meal.";
            case "READY"            -> "Your order is ready!";
            case "OUT_FOR_DELIVERY" -> "Your order is on its way.";
            case "DELIVERED"        -> "Delivered! Enjoy your meal.";
            case "CANCELLED"        -> "Your order has been cancelled.";
            default                 -> "Order status updated.";
        };
    }

    @lombok.Builder @lombok.Data
    public static class OrderUpdatePayload {
        private String orderId;
        private String orderNumber;
        private String status;
        private String previousStatus;
        private Integer estimatedMinutes;
        private String message;
        private String timestamp;
    }
}
