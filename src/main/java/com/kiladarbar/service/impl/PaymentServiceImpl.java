package com.kiladarbar.service.impl;

import com.kiladarbar.dto.request.InitiatePaymentRequest;
import com.kiladarbar.dto.request.VerifyPaymentRequest;
import com.kiladarbar.dto.response.PaymentInitResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.exception.ResourceNotFoundException;
import com.kiladarbar.model.entity.Order;
import com.kiladarbar.repository.OrderRepository;
import com.kiladarbar.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;

    @Override
    public PaymentInitResponse initiatePayment(InitiatePaymentRequest request, UUID userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        // In prod: create Razorpay order via Razorpay SDK
        return PaymentInitResponse.builder()
                .razorpayOrderId("order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .razorpayKeyId("rzp_test_placeholder")
                .amount(order.getTotalAmount())
                .currency("INR")
                .name("Kila Darbar")
                .description("Order " + order.getOrderNumber())
                .build();
    }

    @Override
    public void verifyAndConfirmPayment(VerifyPaymentRequest request) {
        // In prod: verify Razorpay HMAC signature
        log.info("Payment verified: {}", request.getRazorpayPaymentId());
    }

    @Override
    public void handleRazorpayWebhook(String payload, String signature) {
        log.info("Razorpay webhook received");
    }

    @Override
    public String generateUpiQr(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return "upi://pay?pa=kiladarbar@upi&am=" + order.getTotalAmount() + "&tn=" + order.getOrderNumber();
    }
}
