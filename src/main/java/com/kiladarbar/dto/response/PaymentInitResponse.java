package com.kiladarbar.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data @Builder
public class PaymentInitResponse {
    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String name;
    private String description;
    private String prefillPhone;
    private String prefillEmail;
}
