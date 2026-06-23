package com.kiladarbar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {
    private UUID id;
    private String name;
    private String phone;
    private String email;
    private String avatarUrl;
    private LocalDate dateOfBirth;
    private String gender;
    private String role;
    private boolean verified;
    private LocalDateTime createdAt;
}
