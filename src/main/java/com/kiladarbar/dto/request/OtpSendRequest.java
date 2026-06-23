package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OtpSendRequest {
    @NotBlank
    @Pattern(
        regexp  = "^(\\+91[6-9]\\d{9}|[6-9]\\d{9})$",
        message = "Phone must be a valid Indian number (10 digits or +91 prefix)"
    )
    private String phone;

    private String purpose = "LOGIN";
}
