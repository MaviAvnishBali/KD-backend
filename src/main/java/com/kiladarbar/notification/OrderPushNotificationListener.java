package com.kiladarbar.notification;

import com.kiladarbar.event.OrderStatusChangedEvent;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns order lifecycle events into push notifications — the transactional half
 * of the notification system. Runs alongside {@code OrderWebSocketService}:
 * WebSocket updates the app while it's open, this reaches the device when it
 * isn't. Payload keys match the mobile {@code KilaFirebaseMessagingService}
 * contract.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPushNotificationListener {

    private final FcmService fcmService;

    @Async
    @EventListener
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        if (order.getUser() == null) return;

        String stage = mapStage(event.getNewStatus());
        if (stage == null) return; // not a push-worthy transition

        Map<String, String> data = new HashMap<>();
        data.put("type", "order");
        data.put("order_id", order.getId().toString());
        data.put("stage", stage);
        data.put("title", "Kila Darbar • Order #" + order.getOrderNumber());
        data.put("body", body(event.getNewStatus()));

        fcmService.sendToUser(order.getUser().getId(), data);
        log.debug("Order push queued: order={} stage={}", order.getId(), stage);
    }

    /** Backend status → mobile OrderStage (KilaNotifier.OrderStage). */
    private String mapStage(OrderStatus status) {
        return switch (status) {
            case CONFIRMED        -> "CONFIRMED";
            case PREPARING        -> "PREPARING";
            case READY            -> "PACKED";
            case OUT_FOR_DELIVERY -> "OUT_FOR_DELIVERY";
            case DELIVERED        -> "DELIVERED";
            default               -> null; // PENDING / CANCELLED / FAILED / REFUNDED: no push here
        };
    }

    private String body(OrderStatus status) {
        return switch (status) {
            case CONFIRMED        -> "Your royal feast is confirmed 👑 The kitchen has your order.";
            case PREPARING        -> "The fire is lit 🔥 Our chef is preparing your meal now.";
            case READY            -> "Packed with care and sealed warm. A rider is being assigned.";
            case OUT_FOR_DELIVERY -> "Your order is on the way 🛵 Track it live, or keep your phone handy.";
            case DELIVERED        -> "Delivered — enjoy your feast ✨ Tap to rate and earn points.";
            default               -> "Your order status has been updated.";
        };
    }
}
