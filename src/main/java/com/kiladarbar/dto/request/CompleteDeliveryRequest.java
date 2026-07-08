package com.kiladarbar.dto.request;

import lombok.Data;

@Data
public class CompleteDeliveryRequest {
    /** OTP shown in the customer's app; optional safety check. */
    private String otp;

    /** Driver confirms cash was collected (COD). */
    private boolean cashCollected;
}
