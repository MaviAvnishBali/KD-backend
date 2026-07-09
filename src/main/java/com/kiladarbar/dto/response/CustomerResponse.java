package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private long totalOrders;
    private BigDecimal totalSpend;
    private int loyaltyPoints;
    private String tier;
    private LocalDateTime createdAt;
    private LocalDateTime lastOrderAt;
}
