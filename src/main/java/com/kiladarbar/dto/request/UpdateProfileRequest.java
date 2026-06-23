package com.kiladarbar.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String name;
    @Email private String email;
    private String gender;
    private LocalDate dateOfBirth;
    private String avatarUrl;
    private LocalDate anniversaryDate;
}
