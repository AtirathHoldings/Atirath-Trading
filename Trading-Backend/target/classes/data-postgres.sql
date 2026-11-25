-- Seed Initial Demo Users
-- Default Password for both: Password@123 / Corporate@123

-- 1. Individual User
INSERT INTO user_accounts (user_id, account_type, email, mobile_number, referral_code, account_status, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (1, 'INDIVIDUAL', 'sairam.v@atirathgroup.com', '9876543210', 'REF_INDIV_01', 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO individual_profiles (user_id, full_name, created_at, updated_at)
VALUES (1, 'Sairam V', NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at, created_at, updated_at)
VALUES (1, '$2a$12$v9eC0R2uXq1oB6i7JbWcceU1y1c0f8m8pZ0l7bC.h2N5Jv.kYp2Q2', NOW(), NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

-- 2. Corporate User
INSERT INTO user_accounts (user_id, account_type, email, mobile_number, referral_code, account_status, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (2, 'CORPORATE', 'corporate@atirathgroup.com', '9811122233', 'REF_CORP_01', 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

INSERT INTO corporate_profiles (user_id, company_name, registration_id, contact_person_name, created_at, updated_at)
VALUES (2, 'Atirath Capital Private Limited', 'U67120MH2021PTC355123', 'Rajesh Sharma', NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO user_credentials (user_id, password_hash, password_changed_at, created_at, updated_at)
VALUES (2, '$2a$12$v9eC0R2uXq1oB6i7JbWcceU1y1c0f8m8pZ0l7bC.h2N5Jv.kYp2Q2', NOW(), NOW(), NOW())
ON CONFLICT (user_id) DO NOTHING;

-- 3. Default Admin Account (admin@atirathgroup.com / Admin@123)
INSERT INTO admin_accounts (admin_id, email, password_hash, admin_name, admin_status, created_at, updated_at)
OVERRIDING SYSTEM VALUE
VALUES (1, 'admin@atirathgroup.com', '$2a$12$xQ0lE6XhE2d1iNf7G/qC5u5tBwV.N7S5m.y1Wb5b6g7c8d9e0f1g2', 'System Administrator', 'ACTIVE', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- 4. Initial Seed for Admin Support Tables
INSERT INTO public.disputes (dispute_reference, raised_by_user_id, dispute_category, dispute_reason, disputed_amount, priority, status)
VALUES ('DSP-2026-001', 1, 'PAYMENT_VERIFICATION', 'Buyer reported payment completed but escrow status is pending hold', 50000.00, 'MEDIUM', 'OPEN')
ON CONFLICT (dispute_reference) DO NOTHING;

INSERT INTO public.support_tickets (ticket_number, user_id, subject, category, description, priority, status)
VALUES ('TKT-2026-101', 1, 'KYC document resubmission request', 'KYC', 'Need to update address proof document for corporate account', 'NORMAL', 'OPEN')
ON CONFLICT (ticket_number) DO NOTHING;

INSERT INTO public.admin_approval_workflows (workflow_code, workflow_name, module, trigger_condition, required_role, min_approvals, is_active)
VALUES 
('WF-TRADE-HIGH-VAL', 'High Value Trade Clearance (> 10 Lakhs)', 'OPERATIONS', 'TRADE_VALUE > 1000000', 'ROLE_ADMIN', 1, true),
('WF-KYC-CORPORATE', 'Corporate KYC Secondary Verification', 'COMPLIANCE', 'ENTITY_TYPE == CORPORATE', 'ROLE_ADMIN', 1, true)
ON CONFLICT (workflow_code) DO NOTHING;

