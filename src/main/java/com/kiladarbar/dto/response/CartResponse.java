package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponse {

    private UUID userId;
    private List<CartItem> items;
    private int itemCount;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal gstAmount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;
    private String appliedCoupon;
    private BigDecimal freeDeliveryAbove;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CartItem {
        private UUID menuItemId;
        private String name;
        private String imageUrl;
        private BigDecimal unitPrice;
        private int quantity;
        private BigDecimal totalPrice;
        private String specialInstruction;
    }
}
