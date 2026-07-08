package com.kiladarbar.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreatePartyHallBookingRequest {
    @NotBlank private String customerName;
    @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") private String customerPhone;
    @Email private String customerEmail;
    @NotBlank private String eventType;
    @Min(1) @Max(100) private int guestCount;
    @NotBlank private String preferredDate;
    @NotBlank private String preferredTime;
    @NotBlank private String packageType;
    private String specialRequests;
}
