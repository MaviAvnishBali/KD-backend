package com.kiladarbar.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;
    private boolean newUser;

    @Data
    @Builder
    public static class UserInfo {
        private UUID id;
        private String name;
        private String phone;
        private String email;
        private String avatarUrl;
        private String role;
        private int loyaltyPoints;
        private boolean verified;
    }
}
