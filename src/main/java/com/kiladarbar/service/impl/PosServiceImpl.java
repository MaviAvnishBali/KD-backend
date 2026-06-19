package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.PosOrderRequest;
import com.kiladarbar.dto.response.PosReceiptResponse;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.service.PosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PosServiceImpl implements PosService {

    private final OrderRepository orderRepository;

    @Override
    public PosReceiptResponse createPosOrder(PosOrderRequest request, UUID cashierId) {
        return PosReceiptResponse.builder()
                .orderNumber("POS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paymentStatus("PENDING").build();
    }

    @Override
    public PosReceiptResponse getReceipt(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return PosReceiptResponse.builder()
                .orderId(order.getId()).orderNumber(order.getOrderNumber())
                .subtotal(order.getSubtotal()).totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt()).build();
    }

    @Override
    public PosReceiptResponse recordPayment(UUID orderId, String method, String reference) {
        return getReceipt(orderId);
    }

    @Override
    public Object splitBill(UUID orderId, int splitBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getTotalAmount() != null && splitBy > 0) {
            java.math.BigDecimal share = order.getTotalAmount()
                    .divide(java.math.BigDecimal.valueOf(splitBy), 2, java.math.RoundingMode.HALF_UP);
            return java.util.Map.of("splitBy", splitBy, "amountEach", share);
        }
        return java.util.Map.of();
    }

    @Override
    public void voidOrder(UUID orderId, String managerPin, String reason) {
        // In prod: verify managerPin, then void
    }

    @Override
    public Object getTableStatus(UUID branchId) { return List.of(); }

    @Override
    public Object getDayEndSummary(UUID branchId) { return java.util.Map.of(); }

    @Override
    public String generateUpiQr(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return "upi://pay?pa=kiladarbar@upi&am=" + order.getTotalAmount();
    }
}
