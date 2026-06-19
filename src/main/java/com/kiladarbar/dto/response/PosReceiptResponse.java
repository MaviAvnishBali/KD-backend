package com.kiladarbar.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class PosReceiptResponse {
    private UUID orderId;
    private String orderNumber;
    private String tableNumber;
    private LocalDateTime createdAt;
    private List<OrderResponse.OrderItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String cashierName;
}
