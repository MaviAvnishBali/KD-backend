-- V3: Add image_url, pricing and type details to offer_banners

ALTER TABLE offer_banners
    ADD COLUMN IF NOT EXISTS image_url      TEXT,
    ADD COLUMN IF NOT EXISTS badge_text     VARCHAR(30),
    ADD COLUMN IF NOT EXISTS discount_type  VARCHAR(15) DEFAULT 'FLAT',
    ADD COLUMN IF NOT EXISTS discount_value DECIMAL(8,2),
    ADD COLUMN IF NOT EXISTS min_order_amount DECIMAL(10,2) DEFAULT 0,
    ADD COLUMN IF NOT EXISTS max_discount   DECIMAL(10,2);

-- Clear old seeded rows and re-seed with full data
DELETE FROM offer_banners;

INSERT INTO offer_banners (
    emoji, title, description, promo_code, saving_text, badge_text,
    bg_color_start, bg_color_end,
    discount_type, discount_value, min_order_amount, max_discount,
    image_url, display_order, valid_until
) VALUES
(
    '🎉', 'Welcome Gift',
    'Flat 20% off on your very first order with us',
    'WELCOME20', 'Save up to ₹200', 'NEW USER',
    '#6B0F1A', '#3D0409',
    'PERCENTAGE', 20, 0, 200,
    'https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=800&q=80&fit=crop',
    1, NOW() + INTERVAL '30 days'
),
(
    '👑', 'Weekend Royal Feast',
    '₹50 instant discount on orders above ₹500',
    'ROYAL50', 'Save ₹50', 'WEEKEND',
    '#4A148C', '#2A0A5E',
    'FLAT', 50, 500, 50,
    'https://images.unsplash.com/photo-1567188040759-fb8a883dc6d6?w=800&q=80&fit=crop',
    2, NOW() + INTERVAL '7 days'
),
(
    '✨', 'Double Loyalty Points',
    'Earn 2× Royal Rewards points on every Tuesday order',
    'TUESDAY2X', '2× Points', 'EVERY TUESDAY',
    '#1A237E', '#0D1547',
    'LOYALTY', 2, 0, NULL,
    'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=800&q=80&fit=crop',
    3, NOW() + INTERVAL '60 days'
),
(
    '🎁', 'Refer a Friend',
    'You and your friend both get ₹100 wallet credit',
    'REFER100', '₹100 each', 'REFERRAL',
    '#1B4332', '#0F2922',
    'WALLET', 100, 0, 100,
    'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=800&q=80&fit=crop',
    4, NOW() + INTERVAL '90 days'
),
(
    '🍽️', 'Family Feast',
    'Special combo for 4 — flat 15% off, no minimum order',
    'FAMILY15', 'Save 15%', 'COMBO DEAL',
    '#7B3F00', '#4A2600',
    'PERCENTAGE', 15, 0, 300,
    'https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=800&q=80&fit=crop',
    5, NOW() + INTERVAL '14 days'
);
