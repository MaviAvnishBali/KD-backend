package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverAvailabilityRequest {
    @NotNull
    private Boolean available;
}
