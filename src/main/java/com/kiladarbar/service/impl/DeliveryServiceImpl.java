package com.kiladarbar.service.impl;

import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.DeliveryAssignment;
import com.kiladarbar.model.entity.DeliveryPartner;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.repository.DeliveryPartnerRepository;
import com.kiladarbar.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    public void autoAssignDriver(Order order) {
        if (order.getBranch() == null) return;
        List<DeliveryPartner> available = deliveryPartnerRepository
                .findByBranchIdAndAvailableTrue(order.getBranch().getId());
        if (!available.isEmpty()) {
            assignSpecificDriver(order, available.get(0).getId());
        } else {
            log.warn("No available drivers for order: {}", order.getOrderNumber());
        }
    }

    @Override
    public void assignSpecificDriver(Order order, UUID driverId) {
        log.info("Assigned driver {} to order {}", driverId, order.getOrderNumber());
    }
}
