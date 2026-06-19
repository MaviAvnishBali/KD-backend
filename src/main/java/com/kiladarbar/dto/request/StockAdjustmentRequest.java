package com.kiladarbar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockAdjustmentRequest {
    @NotNull @DecimalMin("0.01") private BigDecimal quantity;
    private String reason;
    private String reference;
}
