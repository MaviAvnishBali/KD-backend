-- Kila Darbar Database Schema
-- V1: Initial schema creation
-- PostgreSQL 16

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Branches
CREATE TABLE branches (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) UNIQUE NOT NULL,
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    pincode         VARCHAR(10) NOT NULL,
    latitude        DECIMAL(10,8),
    longitude       DECIMAL(11,8),
    phone           VARCHAR(15) NOT NULL,
    email           VARCHAR(100),
    gstin           VARCHAR(15),
    fssai_no        VARCHAR(20),
    opening_time    TIME NOT NULL DEFAULT '11:00:00',
    closing_time    TIME NOT NULL DEFAULT '23:00:00',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    delivery_radius_km DECIMAL(5,2) DEFAULT 10.00,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Roles
CREATE TABLE roles (
    id          SMALLSERIAL PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO roles (name, description) VALUES
    ('CUSTOMER', 'App customer'),
    ('OWNER', 'Restaurant owner with full access'),
    ('MANAGER', 'Branch manager'),
    ('CASHIER', 'POS cashier'),
    ('CHEF', 'Kitchen chef'),
    ('KITCHEN_STAFF', 'Kitchen support staff'),
    ('DELIVERY_PARTNER', 'Delivery executive'),
    ('CUSTOMER_SUPPORT', 'Customer support agent'),
    ('SUPER_ADMIN', 'Platform super administrator');

-- Permissions
CREATE TABLE permissions (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    module      VARCHAR(50) NOT NULL,
    action      VARCHAR(20) NOT NULL,
    description TEXT
);

CREATE TABLE role_permissions (
    role_id       SMALLINT REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INT REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Users
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone           VARCHAR(15) UNIQUE,
    email           VARCHAR(100) UNIQUE,
    password_hash   VARCHAR(255),
    name            VARCHAR(100),
    avatar_url      TEXT,
    date_of_birth   DATE,
    anniversary_date DATE,
    gender          VARCHAR(10),
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_guest        BOOLEAN NOT NULL DEFAULT FALSE,
    role_id         SMALLINT NOT NULL REFERENCES roles(id) DEFAULT 1,
    google_id       VARCHAR(100) UNIQUE,
    apple_id        VARCHAR(100) UNIQUE,
    fcm_token       TEXT,
    last_login_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_email ON users(email);

-- OTPs
CREATE TABLE user_otps (
    id          BIGSERIAL PRIMARY KEY,
    phone       VARCHAR(15) NOT NULL,
    otp_hash    VARCHAR(64) NOT NULL,
    purpose     VARCHAR(20) NOT NULL,
    attempts    SMALLINT NOT NULL DEFAULT 0,
    is_used     BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otps_phone_expires ON user_otps(phone, expires_at) WHERE NOT is_used;

-- Addresses
CREATE TABLE user_addresses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    label           VARCHAR(50),
    address_line1   VARCHAR(255) NOT NULL,
    address_line2   VARCHAR(255),
    landmark        VARCHAR(100),
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    pincode         VARCHAR(10) NOT NULL,
    latitude        DECIMAL(10,8),
    longitude       DECIMAL(11,8),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Wallets
CREATE TABLE user_wallets (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    balance     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE wallet_transactions (
    id              BIGSERIAL PRIMARY KEY,
    wallet_id       UUID NOT NULL REFERENCES user_wallets(id),
    amount          DECIMAL(12,2) NOT NULL,
    type            VARCHAR(10) NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    UUID,
    description     TEXT,
    balance_after   DECIMAL(12,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Loyalty
CREATE TABLE loyalty_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    points          INT NOT NULL DEFAULT 0,
    tier            VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    lifetime_points INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE loyalty_transactions (
    id              BIGSERIAL PRIMARY KEY,
    account_id      UUID NOT NULL REFERENCES loyalty_accounts(id),
    points          INT NOT NULL,
    type            VARCHAR(10) NOT NULL,
    reference_type  VARCHAR(30),
    reference_id    UUID,
    description     TEXT,
    balance_after   INT NOT NULL,
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Categories
CREATE TABLE categories (
    id              SERIAL PRIMARY KEY,
    branch_id       UUID REFERENCES branches(id),
    name            VARCHAR(100) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    description     TEXT,
    image_url       TEXT,
    display_order   SMALLINT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id       INT REFERENCES categories(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, slug)
);

-- Menu Items
CREATE TABLE menu_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id           UUID REFERENCES branches(id),
    category_id         INT NOT NULL REFERENCES categories(id),
    name                VARCHAR(150) NOT NULL,
    slug                VARCHAR(150) NOT NULL,
    description         TEXT,
    price               DECIMAL(10,2) NOT NULL,
    discount_price      DECIMAL(10,2),
    cost_price          DECIMAL(10,2),
    food_type           VARCHAR(10) NOT NULL DEFAULT 'VEG',
    hsn_code            VARCHAR(8),
    gst_rate            DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    preparation_time    SMALLINT DEFAULT 20,
    calories            INT,
    is_available        BOOLEAN NOT NULL DEFAULT TRUE,
    is_best_seller      BOOLEAN NOT NULL DEFAULT FALSE,
    is_recommended      BOOLEAN NOT NULL DEFAULT FALSE,
    is_seasonal         BOOLEAN NOT NULL DEFAULT FALSE,
    display_order       SMALLINT NOT NULL DEFAULT 0,
    tags                TEXT[],
    search_vector       TSVECTOR,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(branch_id, slug)
);

CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_search ON menu_items USING GIN(search_vector);
CREATE INDEX idx_menu_items_type ON menu_items(food_type);

-- Item Images
CREATE TABLE item_images (
    id              BIGSERIAL PRIMARY KEY,
    item_id         UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    url             TEXT NOT NULL,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    display_order   SMALLINT NOT NULL DEFAULT 0
);

-- Customizations
CREATE TABLE customization_groups (
    id              SERIAL PRIMARY KEY,
    item_id         UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    type            VARCHAR(10) NOT NULL DEFAULT 'SINGLE',
    is_required     BOOLEAN NOT NULL DEFAULT FALSE,
    min_select      SMALLINT DEFAULT 0,
    max_select      SMALLINT DEFAULT 1,
    display_order   SMALLINT DEFAULT 0
);

CREATE TABLE customization_options (
    id              SERIAL PRIMARY KEY,
    group_id        INT NOT NULL REFERENCES customization_groups(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    additional_price DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    is_available    BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE item_addons (
    id              SERIAL PRIMARY KEY,
    item_id         UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    price           DECIMAL(8,2) NOT NULL,
    is_available    BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tables
CREATE TABLE restaurant_tables (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID NOT NULL REFERENCES branches(id),
    table_number    VARCHAR(10) NOT NULL,
    capacity        SMALLINT NOT NULL,
    section         VARCHAR(50),
    qr_code         TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(branch_id, table_number)
);

-- Reservations
CREATE TABLE reservations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID NOT NULL REFERENCES branches(id),
    user_id         UUID REFERENCES users(id),
    customer_name   VARCHAR(100) NOT NULL,
    customer_phone  VARCHAR(15) NOT NULL,
    table_id        UUID REFERENCES restaurant_tables(id),
    party_size      SMALLINT NOT NULL,
    reserved_date   DATE NOT NULL,
    reserved_time   TIME NOT NULL,
    end_time        TIME,
    occasion        VARCHAR(50),
    special_request TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    advance_amount  DECIMAL(10,2) DEFAULT 0.00,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reservations_date ON reservations(branch_id, reserved_date);

-- Orders
CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number        VARCHAR(20) UNIQUE NOT NULL,
    branch_id           UUID NOT NULL REFERENCES branches(id),
    user_id             UUID REFERENCES users(id),
    order_type          VARCHAR(20) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal            DECIMAL(12,2) NOT NULL,
    discount_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    delivery_charge     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    packaging_charge    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tip_amount          DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cgst_amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    sgst_amount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount        DECIMAL(12,2) NOT NULL,
    delivery_address_id UUID REFERENCES user_addresses(id),
    delivery_lat        DECIMAL(10,8),
    delivery_lng        DECIMAL(11,8),
    delivery_instructions TEXT,
    table_id            UUID REFERENCES restaurant_tables(id),
    reservation_id      UUID REFERENCES reservations(id),
    is_scheduled        BOOLEAN NOT NULL DEFAULT FALSE,
    scheduled_at        TIMESTAMPTZ,
    coupon_id           INT,
    coupon_code         VARCHAR(50),
    points_earned       INT NOT NULL DEFAULT 0,
    points_redeemed     INT NOT NULL DEFAULT 0,
    confirmed_at        TIMESTAMPTZ,
    preparing_at        TIMESTAMPTZ,
    ready_at            TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    cancelled_at        TIMESTAMPTZ,
    cancellation_reason TEXT,
    is_pos_order        BOOLEAN NOT NULL DEFAULT FALSE,
    cashier_id          UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_branch_status ON orders(branch_id, status);
CREATE INDEX idx_orders_created ON orders(created_at DESC);
CREATE INDEX idx_orders_branch_date ON orders(branch_id, created_at);

-- Order Items
CREATE TABLE order_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id),
    name            VARCHAR(150) NOT NULL,
    quantity        SMALLINT NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    gst_rate        DECIMAL(5,2) NOT NULL,
    total_price     DECIMAL(12,2) NOT NULL,
    special_instruction TEXT,
    kds_status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    kds_station     VARCHAR(30)
);

CREATE TABLE order_item_customizations (
    id              BIGSERIAL PRIMARY KEY,
    order_item_id   UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    group_name      VARCHAR(100) NOT NULL,
    option_name     VARCHAR(100) NOT NULL,
    additional_price DECIMAL(8,2) NOT NULL DEFAULT 0.00
);

CREATE TABLE order_item_addons (
    id              BIGSERIAL PRIMARY KEY,
    order_item_id   UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    addon_name      VARCHAR(100) NOT NULL,
    addon_price     DECIMAL(8,2) NOT NULL,
    quantity        SMALLINT NOT NULL DEFAULT 1
);

-- Payments
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES orders(id),
    user_id             UUID REFERENCES users(id),
    amount              DECIMAL(12,2) NOT NULL,
    currency            VARCHAR(3) NOT NULL DEFAULT 'INR',
    method              VARCHAR(30) NOT NULL,
    gateway             VARCHAR(20),
    gateway_order_id    VARCHAR(100),
    gateway_payment_id  VARCHAR(100),
    gateway_signature   VARCHAR(255),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    refund_id           VARCHAR(100),
    refund_amount       DECIMAL(12,2),
    refunded_at         TIMESTAMPTZ,
    metadata            JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Delivery Partners
CREATE TABLE delivery_partners (
    id              UUID NOT NULL REFERENCES users(id) PRIMARY KEY,
    vehicle_type    VARCHAR(20),
    vehicle_number  VARCHAR(20),
    license_number  VARCHAR(20),
    branch_id       UUID REFERENCES branches(id),
    is_available    BOOLEAN NOT NULL DEFAULT TRUE,
    current_lat     DECIMAL(10,8),
    current_lng     DECIMAL(11,8),
    last_location_at TIMESTAMPTZ,
    rating          DECIMAL(3,2) DEFAULT 5.00,
    total_deliveries INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE delivery_assignments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID NOT NULL REFERENCES orders(id),
    partner_id          UUID NOT NULL REFERENCES delivery_partners(id),
    assigned_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    accepted_at         TIMESTAMPTZ,
    picked_up_at        TIMESTAMPTZ,
    delivered_at        TIMESTAMPTZ,
    delivery_distance_km DECIMAL(6,2),
    delivery_duration_min SMALLINT,
    earnings            DECIMAL(10,2),
    proof_image_url     TEXT,
    delivery_otp        VARCHAR(6),
    status              VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    route_polyline      TEXT
);

-- Employees
CREATE TABLE employees (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID UNIQUE NOT NULL REFERENCES users(id),
    branch_id       UUID NOT NULL REFERENCES branches(id),
    employee_code   VARCHAR(20) UNIQUE NOT NULL,
    designation     VARCHAR(100),
    department      VARCHAR(50),
    join_date       DATE NOT NULL,
    salary          DECIMAL(12,2),
    shift_type      VARCHAR(10),
    pan_number      VARCHAR(10),
    bank_account    VARCHAR(20),
    bank_ifsc       VARCHAR(11),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE attendance (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     UUID NOT NULL REFERENCES employees(id),
    date            DATE NOT NULL,
    check_in        TIMESTAMPTZ,
    check_out       TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    overtime_hours  DECIMAL(4,2) DEFAULT 0,
    notes           TEXT,
    UNIQUE(employee_id, date)
);

CREATE TABLE shifts (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     UUID NOT NULL REFERENCES employees(id),
    shift_date      DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    is_confirmed    BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(employee_id, shift_date)
);

-- Inventory
CREATE TABLE inventory_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID NOT NULL REFERENCES branches(id),
    name            VARCHAR(150) NOT NULL,
    sku             VARCHAR(50) UNIQUE NOT NULL,
    category        VARCHAR(50),
    unit            VARCHAR(20) NOT NULL,
    current_stock   DECIMAL(12,4) NOT NULL DEFAULT 0,
    reorder_level   DECIMAL(12,4) NOT NULL DEFAULT 0,
    reorder_quantity DECIMAL(12,4),
    cost_per_unit   DECIMAL(10,4),
    storage_location VARCHAR(50),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE stock_movements (
    id                  BIGSERIAL PRIMARY KEY,
    inventory_item_id   UUID NOT NULL REFERENCES inventory_items(id),
    type                VARCHAR(20) NOT NULL,
    quantity            DECIMAL(12,4) NOT NULL,
    stock_after         DECIMAL(12,4) NOT NULL,
    reference_type      VARCHAR(30),
    reference_id        UUID,
    reason              TEXT,
    performed_by        UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Vendors & POs
CREATE TABLE vendors (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    contact_name    VARCHAR(100),
    phone           VARCHAR(15) NOT NULL,
    email           VARCHAR(100),
    gstin           VARCHAR(15),
    address         TEXT,
    payment_terms   VARCHAR(50),
    credit_limit    DECIMAL(12,2),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_orders (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    po_number       VARCHAR(20) UNIQUE NOT NULL,
    vendor_id       UUID NOT NULL REFERENCES vendors(id),
    branch_id       UUID NOT NULL REFERENCES branches(id),
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    order_date      DATE NOT NULL,
    expected_date   DATE,
    received_date   DATE,
    total_amount    DECIMAL(12,2) NOT NULL,
    notes           TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_items (
    id                  BIGSERIAL PRIMARY KEY,
    po_id               UUID NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    inventory_item_id   UUID NOT NULL REFERENCES inventory_items(id),
    quantity_ordered    DECIMAL(12,4) NOT NULL,
    quantity_received   DECIMAL(12,4) NOT NULL DEFAULT 0,
    unit_price          DECIMAL(10,4) NOT NULL,
    total_price         DECIMAL(12,2) NOT NULL,
    expiry_date         DATE
);

-- Coupons
CREATE TABLE coupons (
    id                  SERIAL PRIMARY KEY,
    code                VARCHAR(50) UNIQUE NOT NULL,
    description         TEXT,
    type                VARCHAR(20) NOT NULL,
    value               DECIMAL(10,2) NOT NULL,
    min_order_amount    DECIMAL(10,2) DEFAULT 0.00,
    max_discount        DECIMAL(10,2),
    applicable_for      VARCHAR(20) DEFAULT 'ALL',
    user_limit          INT,
    total_limit         INT,
    total_used          INT NOT NULL DEFAULT 0,
    start_date          TIMESTAMPTZ NOT NULL,
    end_date            TIMESTAMPTZ NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    item_ids            UUID[],
    category_ids        INT[],
    branch_ids          UUID[],
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_coupons_active ON coupons(code) WHERE is_active = TRUE;

CREATE TABLE coupon_usages (
    id          BIGSERIAL PRIMARY KEY,
    coupon_id   INT NOT NULL REFERENCES coupons(id),
    user_id     UUID NOT NULL REFERENCES users(id),
    order_id    UUID NOT NULL REFERENCES orders(id),
    discount    DECIMAL(10,2) NOT NULL,
    used_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Reviews
CREATE TABLE reviews (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    food_rating     SMALLINT CHECK(food_rating BETWEEN 1 AND 5),
    delivery_rating SMALLINT CHECK(delivery_rating BETWEEN 1 AND 5),
    restaurant_rating SMALLINT CHECK(restaurant_rating BETWEEN 1 AND 5),
    comment         TEXT,
    images          TEXT[],
    is_published    BOOLEAN NOT NULL DEFAULT TRUE,
    ai_sentiment    VARCHAR(20),
    ai_keywords     TEXT[],
    admin_reply     TEXT,
    replied_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Notifications
CREATE TABLE notification_templates (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    channel     VARCHAR(20) NOT NULL,
    subject     VARCHAR(255),
    body        TEXT NOT NULL,
    variables   TEXT[],
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES users(id),
    template_id     INT REFERENCES notification_templates(id),
    channel         VARCHAR(20) NOT NULL,
    title           VARCHAR(255),
    body            TEXT NOT NULL,
    data            JSONB,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMPTZ,
    read_at         TIMESTAMPTZ,
    error_message   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, created_at DESC);

-- Audit Logs
CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(100) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       VARCHAR(100),
    old_values      JSONB,
    new_values      JSONB,
    ip_address      INET,
    user_agent      TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_time ON audit_logs(created_at DESC);

-- Marketing Campaigns
CREATE TABLE marketing_campaigns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(150) NOT NULL,
    type            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    segment         VARCHAR(30),
    target_count    INT,
    sent_count      INT NOT NULL DEFAULT 0,
    open_count      INT NOT NULL DEFAULT 0,
    click_count     INT NOT NULL DEFAULT 0,
    subject         VARCHAR(255),
    content         TEXT NOT NULL,
    scheduled_at    TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Recipe Ingredients (for inventory auto-deduction)
CREATE TABLE recipe_ingredients (
    id                  BIGSERIAL PRIMARY KEY,
    menu_item_id        UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    inventory_item_id   UUID NOT NULL REFERENCES inventory_items(id),
    quantity            DECIMAL(10,4) NOT NULL,
    unit                VARCHAR(20) NOT NULL
);

-- Seed initial branch
INSERT INTO branches (name, slug, address_line1, city, state, pincode, phone, gstin, fssai_no) VALUES
('Kila Darbar - Main Branch', 'main', '123, MG Road', 'Bangalore', 'Karnataka', '560001',
 '+919876543210', '29AAAAA0000A1Z5', '10020041018250');
