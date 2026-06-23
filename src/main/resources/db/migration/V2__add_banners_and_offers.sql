-- V2: Hero banners and offers/promotions tables

CREATE TABLE banners (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title         VARCHAR(120) NOT NULL,
    subtitle      VARCHAR(200),
    tag           VARCHAR(40),
    emoji         VARCHAR(10),
    bg_color_start VARCHAR(9) DEFAULT '#6B0F1A',
    bg_color_end   VARCHAR(9) DEFAULT '#3D0409',
    cta_text      VARCHAR(40),
    cta_link      VARCHAR(200),
    display_order SMALLINT NOT NULL DEFAULT 0,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE offer_banners (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    emoji         VARCHAR(10)  NOT NULL,
    title         VARCHAR(80)  NOT NULL,
    description   VARCHAR(160),
    promo_code    VARCHAR(30),
    saving_text   VARCHAR(40),
    bg_color_start VARCHAR(9) DEFAULT '#6B0F1A',
    bg_color_end   VARCHAR(9) DEFAULT '#3D0409',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    display_order SMALLINT NOT NULL DEFAULT 0,
    valid_until   TIMESTAMPTZ
);

-- Seed hero banners
INSERT INTO banners (title, subtitle, tag, emoji, bg_color_start, bg_color_end, cta_text, cta_link, display_order) VALUES
('Veg Dum-e-Shahi Biryani',  '108 Spices · 6 Hours Slow Cooked',           'Signature', '🍚', '#6B0F1A', '#3D0409', 'Order Now', '/menu?item=veg-dum-biryani',  1),
('Paneer Lababdar',          'Cottage cheese in a velvety royal gravy',     'Royal',     '🧀', '#4A148C', '#2A0A5E', 'Order Now', '/menu?item=paneer-lababdar',   2),
('Dal Makhani',              'Slow-simmered lentils — a timeless classic',  'Heritage',  '🥘', '#1B4332', '#0F2922', 'Order Now', '/menu?item=dal-makhani',       3),
('Shahi Tukda',              'Rose water rabri on golden fried bread',      'Dessert',   '🍮', '#7B3F00', '#4A2600', 'Try Now',   '/menu?item=shahi-tukda',       4);

-- Seed offer banners
INSERT INTO offer_banners (emoji, title, description, promo_code, saving_text, bg_color_start, bg_color_end, display_order) VALUES
('🎉', 'Welcome Offer',       'Get 20% off your very first order',    'WELCOME20',  'Save up to ₹200', '#6B0F1A', '#3D0409', 1),
('👑', 'Weekend Royal Feast', '₹50 off on orders above ₹500',         'ROYAL50',    'Save ₹50',        '#4A148C', '#2A0A5E', 2),
('✨', 'Double Points Day',   'Earn 2× loyalty points every Tuesday', 'TUESDAY2X',  '2× Points',       '#1A237E', '#0D1547', 3),
('🎁', 'Refer a Friend',      'Both of you get ₹100 credit',          'REFER100',   '₹100 each',       '#1B4332', '#0F2922', 4),
('🍽️', 'Family Feast Deal',   'Special combo for 4 — flat 15% off',  'FAMILY15',   'Save 15%',        '#7B3F00', '#4A2600', 5);
