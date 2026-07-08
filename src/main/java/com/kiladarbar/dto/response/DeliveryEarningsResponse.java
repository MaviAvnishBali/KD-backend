package com.kiladarbar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DeliveryEarningsResponse {
    private BigDecimal today;
    private BigDecimal thisWeek;
    private BigDecimal total;
    private long deliveriesToday;
    private long deliveriesThisWeek;
    private int totalDeliveries;
}
