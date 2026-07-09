-- Party hall plan catalog. Previously hardcoded in the customer app; now
-- admin-editable so pricing / capacity / plans can change without an app release.
-- perks is newline-separated free text (one perk per line).
CREATE TABLE party_hall_packages (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    type          VARCHAR(30)  NOT NULL UNIQUE,
    name          VARCHAR(60)  NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    max_guests    INT          NOT NULL DEFAULT 100,
    emoji         VARCHAR(10),
    tagline       VARCHAR(150),
    perks         TEXT,
    featured      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order SMALLINT     NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO party_hall_packages (type, name, price, max_guests, emoji, tagline, perks, featured, display_order) VALUES
('BASIC', 'Basic', 15000, 50, '🎈', 'Intimate celebrations done right',
 E'Up to 50 guests\n3-hour hall booking\nBasic décor & seating\nWelcome drinks', FALSE, 1),
('ROYAL', 'Royal', 28000, 80, '👑', 'Our most-loved package',
 E'Up to 80 guests\n5-hour hall booking\nThemed royal décor\nWelcome drinks + starters\nDedicated event host', TRUE, 2),
('GRAND', 'Grand', 48000, 100, '🏛️', 'The full Mughal darbar experience',
 E'Up to 100 guests\nFull-day hall booking\nPremium décor & stage\nLive counters + multi-cuisine buffet\nPhotographer + event manager', FALSE, 3);
