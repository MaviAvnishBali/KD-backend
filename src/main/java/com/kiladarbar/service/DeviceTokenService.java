package com.kiladarbar.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Registration and lookup of device (FCM) tokens. */
public interface DeviceTokenService {

    /** Upsert a token, (re)binding it to this user. */
    void register(UUID userId, String token, String platform);

    /** Remove a single token (e.g. on logout). */
    void remove(String token);

    /** Remove tokens FCM reported as no longer valid. */
    void removeAll(Collection<String> tokens);

    /** All active tokens for a user. */
    List<String> tokensForUser(UUID userId);

    /** Every registered token (marketing broadcast audience). */
    List<String> allTokens();
}
