package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** An assignment as seen by the delivery partner. */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryOrderResponse {
    private UUID assignmentId;
    private UUID orderId;
    private String orderNumber;
    /** Assignment status: ASSIGNED, ACCEPTED, PICKED_UP, DELIVERED, REJECTED */
    private String status;
    /** Order status for context (CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED) */
    private String orderStatus;

    // What to collect — COD
    private String paymentMethod;
    private BigDecimal amountToCollect;

    // Where to go
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private BigDecimal deliveryLat;
    private BigDecimal deliveryLng;
    private String deliveryInstructions;

    // Restaurant pickup point
    private String branchName;
    private String branchAddress;
    private String branchPhone;

    private List<ItemLine> items;
    private int itemCount;

    private BigDecimal earnings;

    private LocalDateTime assignedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;

    @Data
    @Builder
    public static class ItemLine {
        private String name;
        private int quantity;
    }
}
