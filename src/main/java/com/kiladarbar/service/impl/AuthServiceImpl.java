package com.kiladarbar.service.impl;

import com.kiladarbar.dto.response.AuthResponse;
import com.kiladarbar.exception.BusinessException;
import com.kiladarbar.model.entity.Role;
import com.kiladarbar.model.entity.User;
import com.kiladarbar.repository.RoleRepository;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.security.firebase.FirebaseTokenVerifier;
import com.kiladarbar.security.jwt.JwtService;
import com.kiladarbar.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository        userRepository;
    private final RoleRepository        roleRepository;
    private final JwtService            jwtService;
    private final StringRedisTemplate   redisTemplate;
    private final FirebaseTokenVerifier firebaseVerifier;
    private final PasswordEncoder       passwordEncoder;

    private static final Set<String> ADMIN_ROLES = Set.of("MANAGER", "OWNER", "SUPER_ADMIN");

    private static final String OTP_PREFIX      = "otp:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    /* ── OTP (fallback dev flow) ── */

    @Override
    public void sendOtp(String phone, String purpose) {
        String otp = String.format("%06d", (int)(Math.random() * 900000) + 100000);
        redisTemplate.opsForValue().set(OTP_PREFIX + normalisePhone(phone), otp, 5, TimeUnit.MINUTES);
        log.info("DEV OTP for {} ({}): {}", normalisePhone(phone), purpose, otp);
    }

    @Override
    public AuthResponse verifyOtp(String phone, String otp) {
        String normalised = normalisePhone(phone);
        String stored = redisTemplate.opsForValue().get(OTP_PREFIX + normalised);
        if (stored == null || !stored.equals(otp)) {
            throw new BusinessException("Invalid or expired OTP");
        }
        redisTemplate.delete(OTP_PREFIX + normalised);
        User user = userRepository.findByPhone(normalised).orElseGet(() -> createUser(normalised, null, null));
        return buildAuthResponse(user);
    }

    /* ── Admin password login ── */

    @Override
    public AuthResponse adminLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        String role = user.getRole() != null ? user.getRole().getName() : "";
        if (!ADMIN_ROLES.contains(role)) {
            throw new BusinessException("Access denied — admin accounts only");
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }

        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /* ── Firebase Google Sign-In ── */

    @Override
    public AuthResponse googleLogin(String firebaseIdToken) {
        FirebaseTokenVerifier.FirebaseUserInfo info = firebaseVerifier.verify(firebaseIdToken);
        if (info.getEmail() == null || info.getEmail().isBlank()) {
            throw new BusinessException("Google account has no email address");
        }

        User user = userRepository.findByEmail(info.getEmail()).orElseGet(() -> {
            // Also check by googleId (uid)
            return userRepository.findByGoogleId(info.getUid()).orElseGet(() ->
                createUser(null, info.getEmail(), info.getUid())
            );
        });

        // Update user info from Google
        user.setGoogleId(info.getUid());
        if (user.getName() == null || user.getName().isBlank()) user.setName(info.getDisplayName());
        user.setVerified(info.isEmailVerified());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /* ── Firebase Phone Auth ── */

    public AuthResponse verifyFirebasePhone(String firebaseIdToken) {
        FirebaseTokenVerifier.FirebaseUserInfo info = firebaseVerifier.verify(firebaseIdToken);
        if (info.getPhoneNumber() == null || info.getPhoneNumber().isBlank()) {
            throw new BusinessException("Firebase token has no phone number");
        }

        User user = userRepository.findByPhone(info.getPhoneNumber())
                .orElseGet(() -> createUser(info.getPhoneNumber(), null, null));
        user.setVerified(true);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /* ── Refresh & logout ── */

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) throw new BusinessException("Invalid refresh token");
        User user = userRepository.findById(jwtService.extractUserId(refreshToken))
                .orElseThrow(() -> new BusinessException("User not found"));
        return buildAuthResponse(user);
    }

    @Override
    public void logout(String accessToken) {
        long expiry = jwtService.getExpiry(accessToken);
        if (expiry > 0) redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "1", expiry, TimeUnit.MILLISECONDS);
    }

    /* ── Guest ── */

    @Override
    public AuthResponse createGuestSession(String phone) {
        Role customerRole = customerRole();
        User guest = User.builder()
                .phone("GUEST-" + UUID.randomUUID().toString().substring(0, 8))
                .name("Guest")
                .role(customerRole)
                .verified(false)
                .guest(true)
                .build();
        return buildAuthResponse(userRepository.save(guest));
    }

    /* ── Helpers ── */

    private User createUser(String phone, String email, String googleId) {
        Role role = customerRole();
        return userRepository.save(User.builder()
                .phone(phone)
                .email(email)
                .googleId(googleId)
                .role(role)
                .verified(phone != null)
                .build());
    }

    private Role customerRole() {
        return roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new BusinessException("Role CUSTOMER not found — run DB migrations"));
    }

    private AuthResponse buildAuthResponse(User user) {
        String roleName      = user.getRole() != null ? user.getRole().getName() : "CUSTOMER";
        String accessToken   = jwtService.generateAccessToken(user.getId(), roleName);
        String refreshToken  = jwtService.generateRefreshToken(user.getId());
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

    private String normalisePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() == 12 && digits.startsWith("91")) return "+" + digits;
        if (digits.length() == 10) return "+91" + digits;
        return phone;
    }
}
