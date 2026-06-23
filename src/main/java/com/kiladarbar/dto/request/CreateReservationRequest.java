package com.kiladarbar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateReservationRequest {
    @NotNull  private UUID branchId;
    @NotBlank private String customerName;
    @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") private String customerPhone;
    @Min(1) @Max(50) private int partySize;
    @NotNull private LocalDate reservedDate;
    @NotNull private LocalTime reservedTime;
    private String occasion;
    private String specialRequest;
}
