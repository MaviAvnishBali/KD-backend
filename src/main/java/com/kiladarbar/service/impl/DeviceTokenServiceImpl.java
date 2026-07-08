package com.kiladarbar.service.impl;

import com.kiladarbar.model.entity.DeviceToken;
import com.kiladarbar.repository.DeviceTokenRepository;
import com.kiladarbar.repository.UserRepository;
import com.kiladarbar.service.DeviceTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository repository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void register(UUID userId, String token, String platform) {
        if (token == null || token.isBlank()) return;
        LocalDateTime now = LocalDateTime.now();

        DeviceToken device = repository.findByToken(token).orElseGet(DeviceToken::new);
        device.setToken(token);
        device.setPlatform(platform == null ? "ANDROID" : platform);
        device.setUser(userRepository.getReferenceById(userId));
        device.setUpdatedAt(now);
        if (device.getCreatedAt() == null) device.setCreatedAt(now);

        repository.save(device);
        log.debug("Registered device token for user {}", userId);
    }

    @Override
    @Transactional
    public void remove(String token) {
        repository.deleteByToken(token);
    }

    @Override
    @Transactional
    public void removeAll(Collection<String> tokens) {
        if (tokens != null && !tokens.isEmpty()) repository.deleteByTokenIn(tokens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> tokensForUser(UUID userId) {
        return repository.findByUserId(userId).stream().map(DeviceToken::getToken).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> allTokens() {
        return repository.findAll().stream().map(DeviceToken::getToken).toList();
    }
}
