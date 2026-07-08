package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DriverLocationRequest {
    @NotNull
    private BigDecimal lat;

    @NotNull
    private BigDecimal lng;
}
