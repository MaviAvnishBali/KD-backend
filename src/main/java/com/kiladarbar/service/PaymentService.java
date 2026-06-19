package com.kiladarbar.service;

import com.kiladarbar.dto.request.InitiatePaymentRequest;
import com.kiladarbar.dto.request.VerifyPaymentRequest;
import com.kiladarbar.dto.response.PaymentInitResponse;
import java.util.UUID;

public interface PaymentService {
    PaymentInitResponse initiatePayment(InitiatePaymentRequest request, UUID userId);
    void verifyAndConfirmPayment(VerifyPaymentRequest request);
    void handleRazorpayWebhook(String payload, String signature);
    String generateUpiQr(UUID orderId);
}
