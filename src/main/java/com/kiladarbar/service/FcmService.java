package com.kiladarbar.service;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/** Sends data-only FCM messages to devices. */
public interface FcmService {

    /** True when Firebase is configured and pushes can actually be delivered. */
    boolean isEnabled();

    /**
     * Send a data-only push to every device belonging to {@code userId}.
     * Silently no-ops if Firebase isn't configured or the user has no devices.
     */
    void sendToUser(UUID userId, Map<String, String> data);

    /**
     * Broadcast a data-only push to an explicit set of tokens (batched).
     * @return the number of messages FCM accepted.
     */
    int sendToTokens(Collection<String> tokens, Map<String, String> data);
}
