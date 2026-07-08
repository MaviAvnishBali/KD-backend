package com.kiladarbar.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.kiladarbar.service.DeviceTokenService;
import com.kiladarbar.service.FcmService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Firebase Admin SDK sender. Initialises lazily from the configured service
 * account; if the credentials aren't present (e.g. local dev), pushes are
 * disabled and every call is a safe no-op — the app still boots.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FcmServiceImpl implements FcmService {

    private static final int BATCH = 500; // FCM multicast limit

    private final DeviceTokenService deviceTokenService;
    private final ResourceLoader resourceLoader;

    // Read the env var first (set in docker-compose), falling back to the yaml key.
    @Value("${FIREBASE_CREDENTIALS_PATH:${spring.firebase.credentials-path:}}")
    private String credentialsPath;

    private FirebaseMessaging messaging; // null when Firebase isn't configured

    @PostConstruct
    void init() {
        try {
            if (credentialsPath == null || credentialsPath.isBlank()) {
                log.warn("FCM disabled — no firebase.credentials-path configured");
                return;
            }
            Resource resource = resourceLoader.getResource(credentialsPath);
            if (!resource.exists()) {
                log.warn("FCM disabled — credentials not found at {}", credentialsPath);
                return;
            }
            FirebaseApp app;
            if (FirebaseApp.getApps().isEmpty()) {
                try (InputStream in = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(in))
                            .build();
                    app = FirebaseApp.initializeApp(options);
                }
            } else {
                app = FirebaseApp.getInstance();
            }
            messaging = FirebaseMessaging.getInstance(app);
            log.info("FCM push notifications initialised");
        } catch (Exception e) {
            log.error("FCM initialisation failed — pushes disabled", e);
        }
    }

    @Override
    public boolean isEnabled() {
        return messaging != null;
    }

    @Override
    public void sendToUser(UUID userId, Map<String, String> data) {
        sendToTokens(deviceTokenService.tokensForUser(userId), data);
    }

    @Override
    public int sendToTokens(Collection<String> tokens, Map<String, String> data) {
        if (messaging == null || tokens == null || tokens.isEmpty()) return 0;

        AndroidConfig android = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

        List<String> all = new ArrayList<>(tokens);
        List<String> invalid = new ArrayList<>();
        int sent = 0;

        for (int start = 0; start < all.size(); start += BATCH) {
            List<String> batch = all.subList(start, Math.min(start + BATCH, all.size()));
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .putAllData(data)
                    .setAndroidConfig(android)
                    .build();
            try {
                BatchResponse response = messaging.sendEachForMulticast(message);
                sent += response.getSuccessCount();
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException ex = responses.get(i).getException();
                        MessagingErrorCode code = ex != null ? ex.getMessagingErrorCode() : null;
                        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                            invalid.add(batch.get(i)); // stale token — prune it
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.warn("FCM multicast batch failed: {}", e.getMessage());
            }
        }

        if (!invalid.isEmpty()) {
            deviceTokenService.removeAll(invalid);
            log.debug("Pruned {} stale token(s)", invalid.size());
        }
        return sent;
    }
}
