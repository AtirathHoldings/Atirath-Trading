-- =========================================================================
-- ATIRATH SHARE MARKET PLATFORM - POSTGRESQL SCHEMA SPECIFICATION
-- Exactly matches: user_accounts, individual_profiles, corporate_profiles,
-- identity_kyc_details, bank_details, account_documents, user_credentials,
-- password_reset_tokens
-- =========================================================================

-- 1. user_accounts — Common Account Table
CREATE TABLE IF NOT EXISTS user_accounts (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('INDIVIDUAL', 'CORPORATE')),
    email VARCHAR(254) NOT NULL UNIQUE,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    referral_code VARCHAR(50),
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (account_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_accounts_email ON user_accounts(email);
CREATE INDEX IF NOT EXISTS idx_user_accounts_mobile ON user_accounts(mobile_number);

-- 2. individual_profiles — Individual Account
CREATE TABLE IF NOT EXISTS individual_profiles (
    individual_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    full_name VARCHAR(150) NOT NULL,
    company_name VARCHAR(200),
    gender VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 3. corporate_profiles — Corporate Account
CREATE TABLE IF NOT EXISTS corporate_profiles (
    corporate_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    company_name VARCHAR(200) NOT NULL,
    registration_id VARCHAR(30) NOT NULL UNIQUE,
    contact_person_name VARCHAR(150) NOT NULL,
    industry VARCHAR(150),
    incorporation_date DATE,
    company_type VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. identity_kyc_details — Identity Details
CREATE TABLE IF NOT EXISTS identity_kyc_details (
    kyc_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    pan_number VARCHAR(10) NOT NULL,
    aadhaar_number_encrypted TEXT NOT NULL,
    pan_full_name VARCHAR(150) NOT NULL,
    date_of_birth DATE NOT NULL,
    company_pan_number VARCHAR(10),
    gst_number VARCHAR(15),
    tan_number VARCHAR(10),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_kyc_user_id ON identity_kyc_details(user_id);
CREATE INDEX IF NOT EXISTS idx_kyc_pan ON identity_kyc_details(pan_number);

-- 5. bank_details — Bank Account
CREATE TABLE IF NOT EXISTS bank_details (
    bank_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    bank_name VARCHAR(150) NOT NULL,
    account_holder_name VARCHAR(150) NOT NULL,
    account_number_encrypted TEXT NOT NULL,
    ifsc_code VARCHAR(11) NOT NULL,
    branch_name VARCHAR(150) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bank_user_id ON bank_details(user_id);

-- 6. account_documents — Documents & Live Photo
CREATE TABLE IF NOT EXISTS account_documents (
    document_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    document_type VARCHAR(30) NOT NULL CHECK (document_type IN ('ADDRESS_PROOF', 'IDENTITY_PROOF', 'BANK_PASSBOOK', 'LIVE_PHOTO')),
    document_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_doc_user_id ON account_documents(user_id);

-- 7. user_credentials — Password Table
CREATE TABLE IF NOT EXISTS user_credentials (
    credential_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    password_changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_cred_user_id ON user_credentials(user_id);

-- 8. password_reset_tokens — Email Reset Link
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    reset_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reset_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_reset_user_id ON password_reset_tokens(user_id);

-- 9. user_biometric_credentials — Mobile Biometric Authentication
CREATE TABLE IF NOT EXISTS user_biometric_credentials (
    credential_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    device_id VARCHAR(100) NOT NULL,
    device_name VARCHAR(100),
    public_key TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMPTZ,
    CONSTRAINT uq_biometric_user_device UNIQUE(user_id, device_id)
);

CREATE INDEX IF NOT EXISTS idx_biometric_device ON user_biometric_credentials(device_id);
CREATE INDEX IF NOT EXISTS idx_biometric_user_id ON user_biometric_credentials(user_id);

-- =========================================================
-- MODULE A: ATIRATH GROUP - INTERNAL MARKET MODULE
-- PostgreSQL DDL based on supplied Internal Market schema
-- =========================================================

-- 1. Market companies
CREATE TABLE IF NOT EXISTS market_companies (
    company_id BIGSERIAL PRIMARY KEY,
    company_code VARCHAR(30) NOT NULL UNIQUE,
    company_name VARCHAR(200) NOT NULL,
    symbol VARCHAR(30) NOT NULL UNIQUE,
    sector VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),
    founded_date DATE,
    headquarters VARCHAR(200),
    website_url TEXT,
    logo_url TEXT,
    issued_shares BIGINT NOT NULL DEFAULT 0 CHECK (issued_shares >= 0),
    available_shares BIGINT NOT NULL DEFAULT 0 CHECK (available_shares >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (available_shares <= issued_shares)
);

-- 2. Historical market prices / OHLC
CREATE TABLE IF NOT EXISTS market_price_history (
    price_history_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    interval_type VARCHAR(20) NOT NULL,
    price_time TIMESTAMPTZ NOT NULL,
    open_price NUMERIC(18,4) NOT NULL CHECK (open_price >= 0),
    high_price NUMERIC(18,4) NOT NULL CHECK (high_price >= 0),
    low_price NUMERIC(18,4) NOT NULL CHECK (low_price >= 0),
    close_price NUMERIC(18,4) NOT NULL CHECK (close_price >= 0),
    volume_shares BIGINT NOT NULL DEFAULT 0 CHECK (volume_shares >= 0),
    turnover_value NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (turnover_value >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (high_price >= low_price)
);

-- 3. Company leadership
CREATE TABLE IF NOT EXISTS company_leadership (
    leadership_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    person_name VARCHAR(150) NOT NULL,
    designation VARCHAR(150) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 1 CHECK (display_order >= 1),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Company highlights
CREATE TABLE IF NOT EXISTS company_highlights (
    highlight_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    highlight_text TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 1 CHECK (display_order >= 1),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 5. Company shareholding pattern
CREATE TABLE IF NOT EXISTS company_shareholding (
    shareholding_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    holder_category VARCHAR(80) NOT NULL
        CHECK (holder_category IN ('PROMOTERS', 'GROUP_COMPANIES', 'EMPLOYEES', 'OTHERS')),
    shares_held BIGINT NOT NULL CHECK (shares_held >= 0),
    percentage NUMERIC(7,4) NOT NULL CHECK (percentage >= 0 AND percentage <= 100),
    as_of_date DATE
);

-- 6A. Annual company financials
CREATE TABLE IF NOT EXISTS company_annual_financials (
    financial_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    financial_year VARCHAR(20) NOT NULL,
    period_end_date DATE,
    revenue NUMERIC(20,2),
    ebitda NUMERIC(20,2),
    ebitda_margin NUMERIC(10,4),
    net_profit NUMERIC(20,2),
    net_profit_margin NUMERIC(10,4),
    total_assets NUMERIC(20,2),
    total_equity NUMERIC(20,2),
    debt_to_equity NUMERIC(10,4),
    current_ratio NUMERIC(10,4),
    roe NUMERIC(10,4),
    roce NUMERIC(10,4),
    eps NUMERIC(18,4),
    pe_ratio NUMERIC(10,4),
    book_value NUMERIC(18,4),
    price_to_book NUMERIC(10,4),
    dividend_yield NUMERIC(10,4),
    UNIQUE (company_id, financial_year)
);

-- 6B. Quarterly company financials
CREATE TABLE IF NOT EXISTS company_quarterly_financials (
    financial_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    financial_year VARCHAR(20) NOT NULL,
    period_label VARCHAR(30) NOT NULL,
    period_end_date DATE,
    revenue NUMERIC(20,2),
    ebitda NUMERIC(20,2),
    ebitda_margin NUMERIC(10,4),
    net_profit NUMERIC(20,2),
    net_profit_margin NUMERIC(10,4),
    total_assets NUMERIC(20,2),
    total_equity NUMERIC(20,2),
    debt_to_equity NUMERIC(10,4),
    current_ratio NUMERIC(10,4),
    roe NUMERIC(10,4),
    roce NUMERIC(10,4),
    eps NUMERIC(18,4),
    pe_ratio NUMERIC(10,4),
    book_value NUMERIC(18,4),
    price_to_book NUMERIC(10,4),
    dividend_yield NUMERIC(10,4),
    UNIQUE (company_id, financial_year, period_label)
);

-- 7. Current/latest company financial ratios
CREATE TABLE IF NOT EXISTS company_financial_ratios (
    ratio_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    financial_year VARCHAR(20) NOT NULL,
    eps NUMERIC(18,4),
    pe_ratio NUMERIC(10,4),
    book_value NUMERIC(18,4),
    price_to_book NUMERIC(10,4),
    debt_to_equity NUMERIC(10,4),
    current_ratio NUMERIC(10,4),
    roe NUMERIC(10,4),
    roce NUMERIC(10,4),
    dividend_yield NUMERIC(10,4),
    net_profit_margin NUMERIC(10,4),
    ebitda_margin NUMERIC(10,4),
    UNIQUE (company_id, financial_year)
);

-- 8. Company documents
CREATE TABLE IF NOT EXISTS company_documents (
    document_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    category VARCHAR(80) NOT NULL,
    period_date DATE,
    file_url TEXT NOT NULL,
    file_size_bytes BIGINT CHECK (file_size_bytes IS NULL OR file_size_bytes >= 0),
    mime_type VARCHAR(100),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 9. Company announcements
CREATE TABLE IF NOT EXISTS company_announcements (
    announcement_id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    announcement_type VARCHAR(60),
    announcement_date DATE NOT NULL,
    document_id BIGINT
        REFERENCES company_documents(document_id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED'
        CHECK (status IN ('PUBLISHED', 'ARCHIVED'))
);

-- 10. User watchlist
CREATE TABLE IF NOT EXISTS member_watchlists (
    watchlist_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL
        REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, company_id)
);

-- 11. Order matching
CREATE TABLE IF NOT EXISTS market_order_matches (
    match_id BIGSERIAL PRIMARY KEY,
    buy_order_id BIGINT NOT NULL,
    sell_order_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id),
    matched_quantity BIGINT NOT NULL CHECK (matched_quantity > 0),
    matched_price NUMERIC(18,4) NOT NULL CHECK (matched_price >= 0),
    match_status VARCHAR(30) NOT NULL
        CHECK (match_status IN ('MATCHED', 'PARTIAL', 'CANCELLED')),
    matched_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 12. Executed trades
CREATE TABLE IF NOT EXISTS market_trades (
    trade_id BIGSERIAL PRIMARY KEY,
    trade_no VARCHAR(40) NOT NULL UNIQUE,
    match_id BIGINT
        REFERENCES market_order_matches(match_id) ON DELETE SET NULL,
    company_id BIGINT NOT NULL
        REFERENCES market_companies(company_id),
    buy_order_id BIGINT NOT NULL,
    sell_order_id BIGINT NOT NULL,
    buyer_user_id BIGINT NOT NULL
        REFERENCES user_accounts(user_id),
    seller_user_id BIGINT NOT NULL
        REFERENCES user_accounts(user_id),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    trade_price NUMERIC(18,4) NOT NULL CHECK (trade_price >= 0),
    trade_value NUMERIC(20,2) NOT NULL CHECK (trade_value >= 0),
    executed_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EXECUTED'
        CHECK (status IN ('EXECUTED', 'REVERSED'))
);

-- User Share Holdings for portfolio & available sell shares
CREATE TABLE IF NOT EXISTS user_share_holdings (
    holding_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id) ON DELETE CASCADE,
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id) ON DELETE CASCADE,
    total_shares BIGINT NOT NULL DEFAULT 0 CHECK (total_shares >= 0),
    locked_shares BIGINT NOT NULL DEFAULT 0 CHECK (locked_shares >= 0),
    average_buy_price NUMERIC(18,4) DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_company_holding UNIQUE(user_id, company_id)
);

-- =========================================================
-- MODULE B: TRADING & ORDERS
-- =========================================================

CREATE TABLE IF NOT EXISTS market_orders (
    order_id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id),
    order_side VARCHAR(10) NOT NULL CHECK (order_side IN ('BUY', 'SELL')),
    order_type VARCHAR(20) NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT', 'STOP_LOSS')),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    price_per_share NUMERIC(18,4),
    order_value NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (order_value >= 0),
    validity VARCHAR(20) NOT NULL DEFAULT 'DAY_ORDER',
    stop_loss_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    stop_price NUMERIC(18,4),
    trigger_type VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    order_source VARCHAR(30),
    placed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trade_order_charges (
    charge_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES market_orders(order_id) ON DELETE CASCADE,
    charge_type VARCHAR(40) NOT NULL,
    charge_amount NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (charge_amount >= 0),
    tax_amount NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_events (
    event_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES market_orders(order_id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL,
    old_status VARCHAR(30),
    new_status VARCHAR(30),
    event_note TEXT,
    created_by BIGINT REFERENCES user_accounts(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trade_order_documents (
    order_document_id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES market_orders(order_id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    uploaded_by BIGINT REFERENCES user_accounts(user_id),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS share_locks (
    share_lock_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id),
    order_id BIGINT REFERENCES market_orders(order_id) ON DELETE SET NULL,
    locked_quantity BIGINT NOT NULL CHECK (locked_quantity > 0),
    lock_status VARCHAR(20) NOT NULL DEFAULT 'LOCKED',
    locked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS share_transfers (
    transfer_id BIGSERIAL PRIMARY KEY,
    from_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    to_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    trade_id BIGINT REFERENCES market_trades(trade_id),
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    transfer_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    transferred_at TIMESTAMPTZ
);

-- =========================================================
-- MODULE C: NEGOTIATION
-- =========================================================

CREATE TABLE IF NOT EXISTS negotiations (
    negotiation_id BIGSERIAL PRIMARY KEY,
    negotiation_no VARCHAR(40) NOT NULL UNIQUE,
    order_id BIGINT REFERENCES market_orders(order_id) ON DELETE SET NULL,
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id),
    initiator_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    counterparty_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    proposed_price NUMERIC(18,4) NOT NULL CHECK (proposed_price >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CHECK (initiator_user_id <> counterparty_user_id)
);

CREATE TABLE IF NOT EXISTS negotiation_offers (
    offer_id BIGSERIAL PRIMARY KEY,
    negotiation_id BIGINT NOT NULL REFERENCES negotiations(negotiation_id) ON DELETE CASCADE,
    offered_by_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    offered_price NUMERIC(18,4) NOT NULL CHECK (offered_price >= 0),
    offer_message TEXT,
    offer_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS negotiation_messages (
    message_id BIGSERIAL PRIMARY KEY,
    negotiation_id BIGINT NOT NULL REFERENCES negotiations(negotiation_id) ON DELETE CASCADE,
    sender_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    message_text TEXT NOT NULL,
    attachment_url TEXT,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS trade_agreements (
    agreement_id BIGSERIAL PRIMARY KEY,
    agreement_no VARCHAR(40) NOT NULL UNIQUE,
    negotiation_id BIGINT NOT NULL REFERENCES negotiations(negotiation_id),
    buyer_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    seller_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    company_id BIGINT NOT NULL REFERENCES market_companies(company_id),
    agreed_quantity BIGINT NOT NULL CHECK (agreed_quantity > 0),
    agreed_price NUMERIC(18,4) NOT NULL CHECK (agreed_price >= 0),
    agreement_status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    buyer_accepted_at TIMESTAMPTZ,
    seller_accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================================================
-- MODULE D: ESCROW & SETTLEMENT
-- =========================================================

CREATE TABLE IF NOT EXISTS escrow_accounts (
    escrow_account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    account_reference VARCHAR(60) NOT NULL UNIQUE,
    available_balance NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (available_balance >= 0),
    blocked_balance NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (blocked_balance >= 0),
    currency_code CHAR(3) NOT NULL DEFAULT 'INR',
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS escrows (
    escrow_id BIGSERIAL PRIMARY KEY,
    escrow_reference VARCHAR(50) NOT NULL UNIQUE,
    trade_id BIGINT REFERENCES market_trades(trade_id),
    agreement_id BIGINT REFERENCES trade_agreements(agreement_id),
    buyer_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    seller_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    amount NUMERIC(20,2) NOT NULL CHECK (amount > 0),
    escrow_status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    released_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS escrow_payments (
    escrow_payment_id BIGSERIAL PRIMARY KEY,
    escrow_id BIGINT NOT NULL REFERENCES escrows(escrow_id) ON DELETE CASCADE,
    payer_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    payment_reference VARCHAR(100) UNIQUE,
    payment_method VARCHAR(40) NOT NULL,
    amount NUMERIC(20,2) NOT NULL CHECK (amount > 0),
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS settlements (
    settlement_id BIGSERIAL PRIMARY KEY,
    settlement_reference VARCHAR(50) NOT NULL UNIQUE,
    trade_id BIGINT REFERENCES market_trades(trade_id),
    escrow_id BIGINT REFERENCES escrows(escrow_id),
    buyer_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    seller_user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    gross_amount NUMERIC(20,2) NOT NULL CHECK (gross_amount >= 0),
    charges_amount NUMERIC(20,2) NOT NULL DEFAULT 0 CHECK (charges_amount >= 0),
    net_amount NUMERIC(20,2) NOT NULL CHECK (net_amount >= 0),
    settlement_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    settled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS settlement_documents (
    settlement_document_id BIGSERIAL PRIMARY KEY,
    settlement_id BIGINT NOT NULL REFERENCES settlements(settlement_id) ON DELETE CASCADE,
    document_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    uploaded_by BIGINT REFERENCES user_accounts(user_id),
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- =========================================================
-- MODULE E: NOTIFICATIONS
-- =========================================================

CREATE TABLE IF NOT EXISTS app_notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    ref_id BIGINT,
    ref_typ VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    mode_of_notification VARCHAR(30) NOT NULL DEFAULT 'IN_APP',
    sts VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_dt TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS member_notification_preferences (
    preference_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES user_accounts(user_id),
    mode_of_notification VARCHAR(30) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, mode_of_notification)
);

-- =========================================================
-- MODULE F: ADMIN ACCOUNTS & AUDIT
-- =========================================================

CREATE TABLE IF NOT EXISTS admin_accounts (
    admin_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    admin_name VARCHAR(150) NOT NULL,
    admin_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (admin_status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_accounts_email ON admin_accounts(email);

CREATE TABLE IF NOT EXISTS admin_login_attempts (
    attempt_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id BIGINT REFERENCES admin_accounts(admin_id),
    attempted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address INET
);

CREATE INDEX IF NOT EXISTS idx_admin_attempts_admin_id ON admin_login_attempts(admin_id);

-- DDL Updates for individual_profiles, corporate_profiles, identity_kyc_details
ALTER TABLE public.individual_profiles ADD COLUMN IF NOT EXISTS company_name VARCHAR(200);
ALTER TABLE public.individual_profiles ADD COLUMN IF NOT EXISTS gender VARCHAR(20);

ALTER TABLE public.corporate_profiles ADD COLUMN IF NOT EXISTS industry VARCHAR(150);
ALTER TABLE public.corporate_profiles ADD COLUMN IF NOT EXISTS incorporation_date DATE;
ALTER TABLE public.corporate_profiles ADD COLUMN IF NOT EXISTS company_type VARCHAR(50);

ALTER TABLE public.identity_kyc_details ADD COLUMN IF NOT EXISTS company_pan_number VARCHAR(10);
ALTER TABLE public.identity_kyc_details ADD COLUMN IF NOT EXISTS gst_number VARCHAR(15);
ALTER TABLE public.identity_kyc_details ADD COLUMN IF NOT EXISTS tan_number VARCHAR(10);

-- =========================================================
-- MINIMAL ADMIN SUPPORT TABLES (4 Essential Tables)
-- =========================================================

-- Admin Role Attribute
ALTER TABLE public.admin_accounts ADD COLUMN IF NOT EXISTS role VARCHAR(50) DEFAULT 'SUPER_ADMIN';

-- 1. Operations Disputes
CREATE TABLE IF NOT EXISTS public.disputes (
    dispute_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dispute_reference VARCHAR(50) NOT NULL UNIQUE,
    trade_id BIGINT REFERENCES public.market_trades(trade_id) ON DELETE SET NULL,
    escrow_id BIGINT REFERENCES public.escrows(escrow_id) ON DELETE SET NULL,
    raised_by_user_id BIGINT NOT NULL REFERENCES public.user_accounts(user_id) ON DELETE CASCADE,
    against_user_id BIGINT REFERENCES public.user_accounts(user_id) ON DELETE SET NULL,
    dispute_category VARCHAR(100) NOT NULL DEFAULT 'TRADE_EXECUTION',
    dispute_reason TEXT NOT NULL,
    disputed_amount NUMERIC(18, 4) DEFAULT 0.0000,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_admin_id BIGINT REFERENCES public.admin_accounts(admin_id) ON DELETE SET NULL,
    resolution_notes TEXT,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_disputes_status ON public.disputes(status);
CREATE INDEX IF NOT EXISTS idx_disputes_raised_by ON public.disputes(raised_by_user_id);

-- 2. Operations Support Tickets
CREATE TABLE IF NOT EXISTS public.support_tickets (
    ticket_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES public.user_accounts(user_id) ON DELETE CASCADE,
    subject VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'GENERAL',
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to BIGINT REFERENCES public.admin_accounts(admin_id) ON DELETE SET NULL,
    resolution TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_support_tickets_status ON public.support_tickets(status);
CREATE INDEX IF NOT EXISTS idx_support_tickets_user_id ON public.support_tickets(user_id);

-- 3. System Admin Audit Activity Logs
CREATE TABLE IF NOT EXISTS public.admin_audit_logs (
    log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    admin_id BIGINT REFERENCES public.admin_accounts(admin_id) ON DELETE SET NULL,
    admin_name VARCHAR(150),
    action_type VARCHAR(100) NOT NULL,
    module_name VARCHAR(50) NOT NULL,
    target_entity_type VARCHAR(50),
    target_entity_id VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_module ON public.admin_audit_logs(module_name);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created_at ON public.admin_audit_logs(created_at DESC);

-- 4. System Admin Approval Workflows
CREATE TABLE IF NOT EXISTS public.admin_approval_workflows (
    workflow_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workflow_code VARCHAR(50) NOT NULL UNIQUE,
    workflow_name VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL,
    trigger_condition VARCHAR(255),
    required_role VARCHAR(50) NOT NULL DEFAULT 'ROLE_ADMIN',
    min_approvals INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_approval_workflows_module ON public.admin_approval_workflows(module);




