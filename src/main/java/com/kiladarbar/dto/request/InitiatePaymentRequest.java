package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    @NotNull
    private UUID orderId;

    private String paymentMethod; // RAZORPAY, WALLET, CASH
}
