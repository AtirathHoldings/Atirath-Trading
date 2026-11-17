package com.atirath.trading.modules.profile.controller;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.profile.dto.*;
import com.atirath.trading.modules.profile.service.ProfileSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile & Settings", description = "Endpoints for Profile details, KYC verification, Bank accounts, Security, Active Devices, Notifications, Documents, and Platform Settings")
public class ProfileSettingsController {

    private final ProfileSettingsService profileSettingsService;
    private final JwtTokenProvider jwtTokenProvider;

    // =========================================================================
    // 1. Profile Overview & Edit Sections
    // =========================================================================

    @GetMapping
    @Operation(summary = "Get Member Profile", description = "Retrieves full authenticated member profile details including personal, organization, contact, professional, address, and additional info")
    public ResponseEntity<ApiResponse<MemberProfileDto>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        MemberProfileDto profile = profileSettingsService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Member profile retrieved successfully"));
    }

    @PutMapping("/personal")
    @Operation(summary = "Update Personal Information", description = "Updates personal details (Full Name, DOB, Gender, PAN, Aadhaar, Occupation, LinkedIn)")
    public ResponseEntity<ApiResponse<PersonalInfoDto>> updatePersonalInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PersonalInfoDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        PersonalInfoDto updated = profileSettingsService.updatePersonalInfo(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Personal information updated successfully"));
    }

    @PutMapping("/organization")
    @Operation(summary = "Update Organization Information", description = "Updates company details (Name, Industry, Type, Designation, PAN, CIN, Address, Website)")
    public ResponseEntity<ApiResponse<OrganizationInfoDto>> updateOrganizationInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody OrganizationInfoDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        OrganizationInfoDto updated = profileSettingsService.updateOrganizationInfo(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Organization information updated successfully"));
    }

    @PutMapping("/contact")
    @Operation(summary = "Update Contact Information", description = "Updates contact information (Primary/Alternate Mobile, Alternate Email)")
    public ResponseEntity<ApiResponse<ContactInfoDto>> updateContactInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ContactInfoDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        ContactInfoDto updated = profileSettingsService.updateContactInfo(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Contact information updated successfully"));
    }

    @PutMapping("/professional")
    @Operation(summary = "Update Professional Details", description = "Updates professional details (Experience, Expertise, Interested Sectors, Investment Range & Purpose)")
    public ResponseEntity<ApiResponse<ProfessionalDetailsDto>> updateProfessionalDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ProfessionalDetailsDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        ProfessionalDetailsDto updated = profileSettingsService.updateProfessionalDetails(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Professional details updated successfully"));
    }

    @PutMapping("/address")
    @Operation(summary = "Update Address Information", description = "Updates residential / registered address")
    public ResponseEntity<ApiResponse<AddressInfoDto>> updateAddressInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AddressInfoDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        AddressInfoDto updated = profileSettingsService.updateAddressInfo(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Address updated successfully"));
    }

    @PutMapping("/additional")
    @Operation(summary = "Update Additional Information", description = "Updates language preferences, referral source, and profile visibility")
    public ResponseEntity<ApiResponse<AdditionalInfoDto>> updateAdditionalInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AdditionalInfoDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        AdditionalInfoDto updated = profileSettingsService.updateAdditionalInfo(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Additional information updated successfully"));
    }

    // =========================================================================
    // 2. KYC & Verification
    // =========================================================================

    @GetMapping("/kyc")
    @Operation(summary = "Get KYC & Verification Overview", description = "Retrieves KYC document checklist, completion metrics, verification steps, and audit history")
    public ResponseEntity<ApiResponse<KycVerificationPageDto>> getKycVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        KycVerificationPageDto kyc = profileSettingsService.getKycVerification(userId);
        return ResponseEntity.ok(ApiResponse.success(kyc, "KYC verification details retrieved successfully"));
    }

    @PostMapping("/kyc/documents/{docId}/upload")
    @Operation(summary = "Upload KYC Document", description = "Uploads or updates a required KYC document")
    public ResponseEntity<ApiResponse<KycDocumentItemDto>> uploadKycDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long docId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        Long userId = requireAuthenticatedUserId(authHeader);
        String fileUrl = (payload != null && payload.containsKey("fileUrl")) ? payload.get("fileUrl") : null;
        KycDocumentItemDto doc = profileSettingsService.uploadKycDocument(userId, docId, fileUrl);
        return ResponseEntity.ok(ApiResponse.success(doc, "Document uploaded successfully and submitted for verification"));
    }

    // =========================================================================
    // 3. Bank & Settlement Accounts
    // =========================================================================

    @GetMapping("/bank-accounts")
    @Operation(summary = "Get Linked Bank Accounts", description = "Retrieves linked bank accounts, account guidelines, and settlement info")
    public ResponseEntity<ApiResponse<BankAccountsPageDto>> getBankAccounts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        BankAccountsPageDto page = profileSettingsService.getBankAccountsPage(userId);
        return ResponseEntity.ok(ApiResponse.success(page, "Bank accounts retrieved successfully"));
    }

    @PostMapping("/bank-accounts")
    @Operation(summary = "Add New Bank Account", description = "Links a new Indian bank account for trading settlements and payouts")
    public ResponseEntity<ApiResponse<BankAccountItemDto>> addBankAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AddBankAccountRequestDto input) {
        Long userId = requireAuthenticatedUserId(authHeader);
        BankAccountItemDto added = profileSettingsService.addBankAccount(userId, input);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(added, "Bank account added successfully"));
    }

    @PutMapping("/bank-accounts/{accountId}/primary")
    @Operation(summary = "Set Primary Bank Account", description = "Sets specified account as primary for all settlements, payouts and refunds")
    public ResponseEntity<ApiResponse<BankAccountItemDto>> setPrimaryAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accountId) {
        Long userId = requireAuthenticatedUserId(authHeader);
        BankAccountItemDto updated = profileSettingsService.setPrimaryAccount(userId, accountId);
        return ResponseEntity.ok(ApiResponse.success(updated, updated.getBankName() + " set as primary settlement account"));
    }

    @DeleteMapping("/bank-accounts/{accountId}")
    @Operation(summary = "Remove Bank Account", description = "Deletes a linked bank account")
    public ResponseEntity<ApiResponse<Void>> removeBankAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accountId) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.removeBankAccount(userId, accountId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bank account removed successfully"));
    }

    // =========================================================================
    // 4. Security & Login Activity
    // =========================================================================

    @GetMapping("/security")
    @Operation(summary = "Get Security Overview", description = "Retrieves password status, 2FA settings, active sessions/devices, login history, and security toggles")
    public ResponseEntity<ApiResponse<SecurityOverviewDto>> getSecurityOverview(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        SecurityOverviewDto security = profileSettingsService.getSecurityOverview(userId);
        return ResponseEntity.ok(ApiResponse.success(security, "Security overview retrieved successfully"));
    }

    @PostMapping("/security/change-password")
    @Operation(summary = "Change Password", description = "Updates account password after verifying current password and confirmation match")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequestDto input) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.changePassword(userId, input);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PutMapping("/security/2fa")
    @Operation(summary = "Toggle Two-Factor Authentication", description = "Enables or disables 2FA protection")
    public ResponseEntity<ApiResponse<TwoFactorSettingsDto>> toggleTwoFactor(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam boolean enable) {
        Long userId = requireAuthenticatedUserId(authHeader);
        TwoFactorSettingsDto updated = profileSettingsService.toggleTwoFactor(userId, enable);
        return ResponseEntity.ok(ApiResponse.success(updated, "Two-factor authentication updated"));
    }

    @PutMapping("/security/preferences")
    @Operation(summary = "Update Security Preferences", description = "Updates security toggles (Email on login, Login alerts, Require 2FA, Auto logout)")
    public ResponseEntity<ApiResponse<SecurityPreferencesDto>> updateSecurityPreferences(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SecurityPreferencesDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        SecurityPreferencesDto updated = profileSettingsService.updateSecurityPreferences(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Security preferences saved"));
    }

    @DeleteMapping("/security/sessions/{sessionId}")
    @Operation(summary = "Terminate Session", description = "Terminates a specific remote session/device")
    public ResponseEntity<ApiResponse<Void>> terminateSession(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String sessionId) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.terminateSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Session terminated successfully"));
    }

    @DeleteMapping("/security/sessions")
    @Operation(summary = "Terminate All Other Sessions", description = "Terminates all active sessions except the current session")
    public ResponseEntity<ApiResponse<Void>> terminateAllOtherSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.terminateAllOtherSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All other sessions terminated successfully"));
    }

    // =========================================================================
    // 5. Active Devices
    // =========================================================================

    @GetMapping("/devices")
    @Operation(summary = "Get Active Devices", description = "Retrieves currently logged in / enrolled devices, device limits, security tips, and recent device activity")
    public ResponseEntity<ApiResponse<ActiveDevicesPageDto>> getActiveDevices(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        ActiveDevicesPageDto page = profileSettingsService.getActiveDevicesPage(userId);
        return ResponseEntity.ok(ApiResponse.success(page, "Active devices retrieved successfully"));
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "Revoke Device Access", description = "Revokes access for a specific device")
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String deviceId) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.revokeDevice(userId, deviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Device access revoked successfully"));
    }

    // =========================================================================
    // 6. Notifications
    // =========================================================================

    @GetMapping("/notifications")
    @Operation(summary = "Get Notifications", description = "Retrieves user notifications, unread count, category filters, notification preferences, and channels")
    public ResponseEntity<ApiResponse<NotificationsPageDto>> getNotifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type
    ) {
        Long userId = requireAuthenticatedUserId(authHeader);
        NotificationsPageDto page = profileSettingsService.getNotificationsPage(userId, tab, search, type);
        return ResponseEntity.ok(ApiResponse.success(page, "Notifications retrieved successfully"));
    }

    @PutMapping("/notifications/read-all")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Mark Notification as Read", description = "Marks a specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id) {
        Long userId = requireAuthenticatedUserId(authHeader);
        profileSettingsService.markNotificationAsRead(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/notifications/preferences")
    @Operation(summary = "Update Notification Preferences", description = "Toggles preferences for Trade, Negotiation, Settlement, KYC, Member Requests, System, Security, Marketing")
    public ResponseEntity<ApiResponse<NotificationPreferencesDto>> updateNotificationPreferences(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody NotificationPreferencesDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        NotificationPreferencesDto updated = profileSettingsService.updateNotificationPreferences(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification preferences updated successfully"));
    }

    @PutMapping("/notifications/channels")
    @Operation(summary = "Update Notification Channels", description = "Toggles In-App, Email, and SMS notification delivery channels")
    public ResponseEntity<ApiResponse<NotificationChannelsDto>> updateNotificationChannels(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody NotificationChannelsDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        NotificationChannelsDto updated = profileSettingsService.updateNotificationChannels(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification channels updated successfully"));
    }

    // =========================================================================
    // 7. Documents
    // =========================================================================

    @GetMapping("/documents")
    @Operation(summary = "Get Stored Documents", description = "Retrieves user documents from PostgreSQL, storage usage, guidelines, and expiry alerts")
    public ResponseEntity<ApiResponse<DocumentsPageDto>> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        Long userId = requireAuthenticatedUserId(authHeader);
        DocumentsPageDto page = profileSettingsService.getDocumentsPage(userId, category, search);
        return ResponseEntity.ok(ApiResponse.success(page, "Documents retrieved successfully"));
    }

    @PostMapping("/documents/upload")
    @Operation(summary = "Quick Upload Document", description = "Uploads a new document into user document storage")
    public ResponseEntity<ApiResponse<DocumentRecordDto>> uploadDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> payload
    ) {
        Long userId = requireAuthenticatedUserId(authHeader);
        String name = payload.get("name");
        String category = payload.get("category");
        String fileName = payload.get("fileName");
        String fileUrl = payload.get("fileUrl");
        DocumentRecordDto doc = profileSettingsService.uploadDocument(userId, name, category, fileName, fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doc, "Document uploaded successfully"));
    }

    // =========================================================================
    // 8. Platform Settings
    // =========================================================================

    @GetMapping("/settings")
    @Operation(summary = "Get Platform Settings", description = "Retrieves General settings, Platform preferences, Appearance theme, and Language & Region")
    public ResponseEntity<ApiResponse<PlatformSettingsPageDto>> getPlatformSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        PlatformSettingsPageDto page = profileSettingsService.getPlatformSettingsPage(userId);
        return ResponseEntity.ok(ApiResponse.success(page, "Platform settings retrieved successfully"));
    }

    @PutMapping("/settings/general")
    @Operation(summary = "Update General Settings", description = "Updates email, mobile, company, designation")
    public ResponseEntity<ApiResponse<GeneralSettingsDto>> updateGeneralSettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody GeneralSettingsDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        GeneralSettingsDto updated = profileSettingsService.updateGeneralSettings(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "General settings updated successfully"));
    }

    @PutMapping("/settings/preferences")
    @Operation(summary = "Update Platform Preferences", description = "Updates platform toggles (Email, SMS, Marketing, Show Portfolio Summary, Auto Logout)")
    public ResponseEntity<ApiResponse<PlatformPreferencesDto>> updatePlatformPreferences(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PlatformPreferencesDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        PlatformPreferencesDto updated = profileSettingsService.updatePlatformPreferences(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Platform preferences updated successfully"));
    }

    @PutMapping("/settings/appearance")
    @Operation(summary = "Update Theme Appearance", description = "Selects Light, Dark, or System display theme")
    public ResponseEntity<ApiResponse<AppearanceSettingsDto>> updateAppearance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AppearanceSettingsDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        AppearanceSettingsDto updated = profileSettingsService.updateAppearance(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Appearance theme updated to " + updated.getSelectedTheme()));
    }

    @PutMapping("/settings/language-region")
    @Operation(summary = "Update Language & Region", description = "Updates preferred language and timezone")
    public ResponseEntity<ApiResponse<LanguageRegionDto>> updateLanguageRegion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LanguageRegionDto update) {
        Long userId = requireAuthenticatedUserId(authHeader);
        LanguageRegionDto updated = profileSettingsService.updateLanguageRegion(userId, update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Language & Region settings updated"));
    }

    @GetMapping("/settings/export")
    @Operation(summary = "Export Account Data", description = "Generates account data export metadata for the authenticated user")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportAccountData(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        return ResponseEntity.ok(ApiResponse.success(
                profileSettingsService.exportAccountData(userId),
                "Account data export generated successfully"
        ));
    }

    @GetMapping("/settings/download-statements")
    @Operation(summary = "Download Trade Statements", description = "Generates trade statement download metadata for the authenticated user")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadStatements(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = requireAuthenticatedUserId(authHeader);
        return ResponseEntity.ok(ApiResponse.success(
                profileSettingsService.downloadStatements(userId),
                "Trade statements generated successfully"
        ));
    }

    private Long requireAuthenticatedUserId(String authHeader) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new ApiException("Authentication required to access profile settings", HttpStatus.UNAUTHORIZED);
        }
        Long userId = extractUserId(authHeader);
        if (userId == null) {
            throw new ApiException("Authentication required to access profile settings", HttpStatus.UNAUTHORIZED);
        }
        return userId;
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7).trim();
                return jwtTokenProvider.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("[ProfileSettingsController] Could not parse JWT: {}", e.getMessage());
            }
        }
        return null;
    }
}
