package com.kiladarbar.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.kiladarbar.model.enums.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Branch ID is required")
    private UUID branchId;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;

    @JsonAlias("addressId")
    private UUID deliveryAddressId;

    @JsonAlias("instructions")
    private String deliveryInstructions;

    private UUID tableId;
    private String couponCode;
    private Integer redeemPoints;
    private BigDecimal tipAmount;
    private boolean scheduled;
    private LocalDateTime scheduledAt;

    /** Only CASH_ON_DELIVERY (alias COD) is accepted — online payments are disabled. */
    private String paymentMethod;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private UUID menuItemId;

        @NotNull
        private Integer quantity;

        private List<Integer> selectedCustomizationOptionIds;
        private List<Integer> selectedAddonIds;
        private String specialInstruction;
    }
}
