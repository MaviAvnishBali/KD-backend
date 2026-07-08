-- Party hall / banquet booking enquiries submitted from the mobile app.
-- These are inquiry-style bookings: created as PENDING with no price; staff
-- confirm pricing (total_amount) later. preferred_date/time are stored as free
-- text because the app collects them as loose strings (e.g. "7:00 PM").
CREATE TABLE party_hall_bookings (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID         REFERENCES users(id) ON DELETE SET NULL,
    customer_name    VARCHAR(100) NOT NULL,
    customer_phone   VARCHAR(15)  NOT NULL,
    customer_email   VARCHAR(150),
    event_type       VARCHAR(50)  NOT NULL,
    guest_count      INT          NOT NULL,
    preferred_date   VARCHAR(30)  NOT NULL,
    preferred_time   VARCHAR(30)  NOT NULL,
    package_type     VARCHAR(30)  NOT NULL,
    special_requests TEXT,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    total_amount     DECIMAL(10,2),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_party_hall_bookings_user ON party_hall_bookings(user_id, created_at DESC);
