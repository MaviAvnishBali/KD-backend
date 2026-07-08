-- Device (FCM) tokens for push notifications.
-- One row per device; a token is globally unique and re-points to whichever
-- user last registered it (handles shared devices / re-login).
CREATE TABLE device_tokens (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      TEXT        NOT NULL UNIQUE,
    platform   VARCHAR(20) NOT NULL DEFAULT 'ANDROID',
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE INDEX idx_device_tokens_user ON device_tokens (user_id);
