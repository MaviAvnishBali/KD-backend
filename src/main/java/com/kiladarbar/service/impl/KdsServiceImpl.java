package com.kiladarbar.service.impl;

import com.kiladarbar.dto.response.KdsOrderResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.service.KdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KdsServiceImpl implements KdsService {

    private final OrderRepository orderRepository;

    @Override
    public List<KdsOrderResponse> getActiveOrders(UUID branchId, String station) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.PREPARING)
                .filter(o -> branchId == null || (o.getBranch() != null && o.getBranch().getId().equals(branchId)))
                .map(this::toKdsResponse)
                .collect(Collectors.toList());
    }

    @Override
    public KdsOrderResponse startPreparing(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.PREPARING);
        order.setPreparingAt(LocalDateTime.now());
        return toKdsResponse(orderRepository.save(order));
    }

    @Override
    public KdsOrderResponse markReady(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.READY);
        order.setReadyAt(LocalDateTime.now());
        return toKdsResponse(orderRepository.save(order));
    }

    @Override
    public void markItemReady(UUID orderId, UUID itemId) {
        orderRepository.findById(orderId).ifPresent(order ->
            order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> {
                    item.setKdsStatus("READY");
                    orderRepository.save(order);
                })
        );
    }

    @Override
    public Object getKitchenStats(UUID branchId) {
        return java.util.Map.of("activeOrders", getActiveOrders(branchId, null).size());
    }

    private KdsOrderResponse toKdsResponse(Order order) {
        int elapsed = order.getCreatedAt() != null
                ? (int) Duration.between(order.getCreatedAt(), LocalDateTime.now()).toMinutes() : 0;
        List<KdsOrderResponse.KdsItemDto> items = order.getItems().stream()
                .map(i -> KdsOrderResponse.KdsItemDto.builder()
                        .id(i.getId()).name(i.getName()).quantity(i.getQuantity())
                        .station(i.getKdsStation()).status(i.getKdsStatus())
                        .specialInstruction(i.getSpecialInstruction()).build())
                .collect(Collectors.toList());
        return KdsOrderResponse.builder()
                .id(order.getId()).orderNumber(order.getOrderNumber())
                .status(order.getStatus()).orderType(order.getOrderType())
                .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .createdAt(order.getCreatedAt()).elapsedMinutes(elapsed)
                .items(items).build();
    }
}
