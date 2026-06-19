package com.kiladarbar.service;

import com.kiladarbar.model.entity.Order;
import java.util.UUID;

public interface DeliveryService {
    void autoAssignDriver(Order order);
    void assignSpecificDriver(Order order, UUID driverId);
}
