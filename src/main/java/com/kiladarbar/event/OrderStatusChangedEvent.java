package com.kiladarbar.event;

import com.kiladarbar.model.entity.Order;
import com.kiladarbar.model.enums.OrderStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {
    private final Order order;
    private final OrderStatus previousStatus;
    private final OrderStatus newStatus;

    public OrderStatusChangedEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        super(order);
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
