package com.kiladarbar.controller.customer;

import com.kiladarbar.dto.request.InitiatePaymentRequest;
import com.kiladarbar.dto.request.VerifyPaymentRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.dto.response.PaymentInitResponse;
import com.kiladarbar.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay integration and payment verification")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment for an order")
    public ResponseEntity<ApiResponse<PaymentInitResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.initiatePayment(request, UUID.fromString(principal.getUsername()))));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature")
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        paymentService.verifyAndConfirmPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully"));
    }

    @PostMapping("/webhook/razorpay")
    @Operation(summary = "Razorpay webhook handler")
    public ResponseEntity<Void> razorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleRazorpayWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upi-qr/{orderId}")
    @Operation(summary = "Generate UPI QR for an order")
    public ResponseEntity<ApiResponse<String>> generateUpiQr(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.generateUpiQr(orderId)));
    }
}
