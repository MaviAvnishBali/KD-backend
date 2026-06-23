package com.kiladarbar.security.firebase;

import com.kiladarbar.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Verifies Firebase ID tokens via the Identity Toolkit REST API.
 * No Firebase Admin SDK / service-account.json required.
 *
 * Endpoint: POST https://identitytoolkit.googleapis.com/v1/accounts:lookup?key={API_KEY}
 */
@Component
@Slf4j
public class FirebaseTokenVerifier {

    @Value("${app.firebase.web-api-key}")
    private String webApiKey;

    private final RestTemplate rest = new RestTemplate();

    /**
     * Verify any Firebase ID token (Google or Phone) and return user claims.
     */
    @SuppressWarnings("unchecked")
    public FirebaseUserInfo verify(String idToken) {
        String url = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=" + webApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> req = new HttpEntity<>(Map.of("idToken", idToken), headers);

        try {
            ResponseEntity<Map> response = rest.postForEntity(url, req, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("Firebase token verification failed");
            }
            List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody().get("users");
            if (users == null || users.isEmpty()) {
                throw new BusinessException("No user found for Firebase token");
            }
            Map<String, Object> user = users.get(0);
            return FirebaseUserInfo.builder()
                    .uid((String) user.get("localId"))
                    .email((String) user.get("email"))
                    .displayName((String) user.get("displayName"))
                    .phoneNumber((String) user.get("phoneNumber"))
                    .emailVerified(Boolean.TRUE.equals(user.get("emailVerified")))
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Firebase token verification error: {}", e.getMessage());
            throw new BusinessException("Invalid Firebase token: " + e.getMessage());
        }
    }

    @lombok.Builder @lombok.Data
    public static class FirebaseUserInfo {
        private String  uid;
        private String  email;
        private String  displayName;
        private String  phoneNumber;
        private boolean emailVerified;
    }
}
