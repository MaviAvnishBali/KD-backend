package com.kiladarbar.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class GuestLoginRequest {
    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String phone;
}
