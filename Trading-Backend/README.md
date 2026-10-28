# ATIRATH INTERNAL SHARE MARKET PLATFORM - BACKEND

Enterprise-grade Spring Boot 3 Authentication & Security Backend for Atirath Group Internal Share Trading Platform.

---

## 🚀 Key Highlights & Architecture

- **Separate Web & Mobile Authentication Pipelines**:
  - **Member & Admin Web Portal** (`/api/v1/auth/web/*`):
    - Email & Password Login
    - 2FA OTP Email Verification (90-second expiration, 6-digit PIN)
    - Remember-Me Token Persistence
    - Google & Microsoft OAuth 2.0 SSO
  - **Mobile Trading Application** (`/api/v1/auth/mobile/*`):
    - Phone Number (`+91 98765 43210`) / Email dual login tabs
    - SMS OTP Verification (60-second expiration)
    - Hardware Biometric Authentication (FaceID / TouchID challenge-response signing)
    - Device Binding & Extended Mobile Refresh Tokens (30 days)
  - **Shared Token Management** (`/api/v1/auth/*`):
    - Secure JWT (HMAC-SHA256) stateless authentication
    - Token rotation (`/refresh-token`)
    - Session revocation & Logout (`/logout`)
    - Authenticated Profile Retrieval (`/me`) with role-based permissions

---

## 🗄️ Database Setup (PostgreSQL & Dev H2)

### 1. Development Mode (Zero-Config In-Memory Database)
By default, the application runs on an in-memory **H2 Database with PostgreSQL compatibility**. No database installation is required for initial development or testing!
- **H2 Console URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:tradingdb`
- **Username**: `sa`
- **Password**: *(leave blank)*

### 2. PostgreSQL Production Setup (When you create the DB later)
When you are ready to connect to PostgreSQL:
1. Create database:
   ```sql
   CREATE DATABASE atirath_trading_db;
   ```
2. Run `schema-postgres.sql` and `data-postgres.sql` located in `src/main/resources/` (or let Hibernate auto-create tables).
3. Set your active Spring profile to `postgres` in `application.yml` or run with:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   ```
   Or set environment variables:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/atirath_trading_db
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   ```

---

## 🔑 Pre-Seeded Demo Test Accounts

| Role | Email / Login Identifier | Phone Number | Password | Client Access |
| :--- | :--- | :--- | :--- | :--- |
| **Member (Trader)** | `sairam.v@atirathgroup.com` | `+91 9876543210` | `Password@123` | Web & Mobile |
| **Platform Admin** | `admin@atirathgroup.com` | `+91 9811122233` | `Admin@123` | Web & Mobile |
| **Compliance Officer** | `compliance@atirathgroup.com` | `+91 9844455566` | `Password@123` | Web Portal |

> 💡 **Dev Mode OTP**: When running in dev mode, OTP codes are printed to the console log, and `123456` can also be used as a universal testing OTP.

---

## 📡 API Endpoint Summary

### 1. Web Application (`/api/v1/auth/web`)
- `POST /api/v1/auth/web/login` - Initiate Web Login (Email + Password) -> Dispatches 2FA OTP
- `POST /api/v1/auth/web/verify-otp` - Verify 6-digit OTP -> Issues JWT Access + Refresh Tokens
- `POST /api/v1/auth/web/resend-otp` - Resend OTP to email or SMS
- `POST /api/v1/auth/web/oauth/social` - Google / Microsoft Single Sign-On

### 2. Mobile Application (`/api/v1/auth/mobile`)
- `POST /api/v1/auth/mobile/login` - Mobile Login (Mobile Number or Email + Password)
- `POST /api/v1/auth/mobile/verify-otp` - Verify SMS OTP -> Issues 30-day JWT session
- `POST /api/v1/auth/mobile/resend-otp` - Resend SMS OTP
- `POST /api/v1/auth/mobile/biometric/initiate` - Get Biometric Challenge Nonce
- `POST /api/v1/auth/mobile/biometric/verify` - Verify Biometric signature -> Issues JWT directly
- `POST /api/v1/auth/mobile/biometric/enroll` - Enroll device biometric key

### 3. Common / Session Management (`/api/v1/auth`)
- `POST /api/v1/auth/refresh-token` - Refresh expired access token
- `POST /api/v1/auth/logout` - Invalidate session & refresh token
- `GET /api/v1/auth/me` - Get profile & permissions (Requires `Authorization: Bearer <token>`)

---

## 📖 Swagger / OpenAPI Interactive Documentation
Once the server is running, navigate to:
```
http://localhost:8080/swagger-ui.html
```
Interactive testing and full schema documentation are available.
