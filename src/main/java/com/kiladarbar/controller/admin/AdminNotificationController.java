package com.kiladarbar.controller.admin;

import com.kiladarbar.dto.request.CampaignNotificationRequest;
import com.kiladarbar.dto.response.ApiResponse;
import com.kiladarbar.service.DeviceTokenService;
import com.kiladarbar.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin marketing/campaign notifications — broadcasts a data-only push to every
 * registered device. The payload keys mirror the mobile
 * {@code KilaFirebaseMessagingService} contract, so the app renders it as a
 * rich promo/engagement notification.
 */
@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Admin - Notifications", description = "Send marketing push campaigns")
@PreAuthorize("hasAnyRole('MANAGER', 'OWNER', 'SUPER_ADMIN')")
public class AdminNotificationController {

    private final FcmService fcmService;
    private final DeviceTokenService deviceTokenService;

    /** How many devices a broadcast would reach, and whether push is live. */
    @GetMapping("/audience")
    @Operation(summary = "Broadcast audience size & push status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> audience() {
        Map<String, Object> result = new HashMap<>();
        result.put("devices", deviceTokenService.allTokens().size());
        result.put("pushEnabled", fcmService.isEnabled());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /** Compose & send a campaign to all registered devices. */
    @PostMapping("/broadcast")
    @Operation(summary = "Send a marketing push to all devices")
    public ResponseEntity<ApiResponse<Map<String, Object>>> broadcast(
            @Valid @RequestBody CampaignNotificationRequest req) {

        Map<String, String> data = new HashMap<>();
        data.put("type", (req.getType() == null || req.getType().isBlank()) ? "promo" : req.getType());
        data.put("id", String.valueOf(System.currentTimeMillis() & 0xFFFFFFL));
        data.put("title", req.getTitle());
        data.put("body", req.getBody());
        data.put("flash", String.valueOf(req.isFlash()));
        putIfPresent(data, "deep_link", req.getDeepLink());
        putIfPresent(data, "image", req.getImage());
        putIfPresent(data, "cta", req.getCta());
        putIfPresent(data, "channel", req.getChannel());

        List<String> tokens = deviceTokenService.allTokens();
        int sent = fcmService.isEnabled() ? fcmService.sendToTokens(tokens, data) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("devices", tokens.size());
        result.put("dispatched", fcmService.isEnabled());
        result.put("sent", sent);

        String message = fcmService.isEnabled()
                ? "Campaign sent to " + sent + " of " + tokens.size() + " device(s)"
                : "Saved, but push is disabled on the server (no Firebase credentials). Audience: "
                        + tokens.size() + " device(s).";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }
}
