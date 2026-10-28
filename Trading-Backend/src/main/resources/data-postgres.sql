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
