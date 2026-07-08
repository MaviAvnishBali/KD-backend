package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kiladarbar.model.enums.OrderStatus;
import com.kiladarbar.model.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private OrderStatus status;
    private OrderType orderType;
    private String branchName;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal packagingCharge;
    private BigDecimal tipAmount;
    private BigDecimal cgstAmount;
    private BigDecimal sgstAmount;
    private BigDecimal totalAmount;

    // Items
    private List<OrderItemResponse> items;

    // Payment
    private String paymentMethod;
    private String paymentStatus;
    private String couponCode;

    // Delivery
    private DeliveryInfo deliveryInfo;
    private String deliveryAddress;
    private Integer estimatedMinutes;

    // Loyalty
    private int pointsEarned;
    private int pointsRedeemed;

    // Timing
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime readyAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OrderItemResponse {
        private UUID id;
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String kdsStatus;
        private List<String> customizations;
        private List<String> addons;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryInfo {
        private String driverName;
        private String driverPhone;
        private BigDecimal driverLat;
        private BigDecimal driverLng;
        private Integer estimatedMinutes;
        private String trackingUrl;
        /** Share with the driver to confirm hand-over */
        private String deliveryOtp;
    }
}
