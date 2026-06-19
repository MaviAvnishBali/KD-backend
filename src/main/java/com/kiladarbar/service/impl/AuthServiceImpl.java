package com.kiladarbar.service.impl;

import com.kiladarbar.dto.response.AuthResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.model.entity.Role;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.security.jwt.JwtService;
import com.kiladarbar.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    private static final String OTP_PREFIX = "otp:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Override
    public void sendOtp(String phone, String purpose) {
        // Generate 6-digit OTP (in prod: send via SMS)
        String otp = String.format("%06d", (int)(Math.random() * 900000) + 100000);
        redisTemplate.opsForValue().set(OTP_PREFIX + phone, otp, 5, TimeUnit.MINUTES);
        log.info("OTP for {} ({}): {}", phone, purpose, otp); // Dev only — remove in prod
    }

    @Override
    public AuthResponse verifyOtp(String phone, String otp) {
        String stored = redisTemplate.opsForValue().get(OTP_PREFIX + phone);
        if (stored == null || !stored.equals(otp)) {
            throw new BusinessException("Invalid or expired OTP");
        }
        redisTemplate.delete(OTP_PREFIX + phone);

        User user = userRepository.findByPhone(phone).orElseGet(() -> createUser(phone));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse googleLogin(String idToken) {
        // In prod: verify with Firebase/Google; extract email+name
        throw new BusinessException("Google login not configured in dev mode");
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessException("Invalid refresh token");
        }
        UUID userId = jwtService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        return buildAuthResponse(user);
    }

    @Override
    public void logout(String accessToken) {
        long expiry = jwtService.getExpiry(accessToken);
        if (expiry > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "1", expiry, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public AuthResponse createGuestSession(String phone) {
        User guest = User.builder()
                .phone(phone != null ? phone : "GUEST-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Guest")
                .verified(false)
                .build();
        User saved = userRepository.save(guest);
        return buildAuthResponse(saved);
    }

    private User createUser(String phone) {
        return userRepository.save(User.builder()
                .phone(phone)
                .verified(true)
                .build());
    }

    private AuthResponse buildAuthResponse(User user) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "CUSTOMER";
        String accessToken = jwtService.generateAccessToken(user.getId(), roleName);
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .role(roleName)
                        .verified(user.isVerified())
                        .build())
                .build();
    }
}
