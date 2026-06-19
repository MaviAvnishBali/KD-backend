package com.kiladarbar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phone;

    @NotBlank
    @Size(min = 6, max = 6)
    private String otp;
}
