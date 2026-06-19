package com.kiladarbar.service;

import com.kiladarbar.dto.response.AuthResponse;

public interface AuthService {
    void sendOtp(String phone, String purpose);
    AuthResponse verifyOtp(String phone, String otp);
    AuthResponse googleLogin(String idToken);
    AuthResponse refreshToken(String refreshToken);
    void logout(String accessToken);
    AuthResponse createGuestSession(String phone);
}
