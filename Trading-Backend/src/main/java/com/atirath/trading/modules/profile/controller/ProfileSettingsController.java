package com.atirath.trading.modules.profile.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.profile.dto.*;
import com.atirath.trading.modules.profile.service.ProfileSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile & Settings", description = "Endpoints for Profile details, KYC verification, Bank accounts, Security, Active Devices, Notifications, Documents, and Platform Settings")
public class ProfileSettingsController {

    private final ProfileSettingsService profileSettingsService;

    // =========================================================================
    // 1. Profile Overview & Edit Sections
    // =========================================================================

    @GetMapping
    @Operation(summary = "Get Member Profile", description = "Retrieves full profile details for Rohit Sharma (ATG10027) including personal, organization, contact, professional, address, and additional info with 90% completion")
    public ResponseEntity<ApiResponse<MemberProfileDto>> getProfile() {
        MemberProfileDto profile = profileSettingsService.getProfile();
        return ResponseEntity.ok(ApiResponse.success(profile, "Member profile retrieved successfully"));
    }

    @PutMapping("/personal")
    @Operation(summary = "Update Personal Information", description = "Updates personal details (Full Name, DOB, Gender, PAN, Aadhaar, Occupation, LinkedIn)")
    public ResponseEntity<ApiResponse<PersonalInfoDto>> updatePersonalInfo(@RequestBody PersonalInfoDto update) {
        PersonalInfoDto updated = profileSettingsService.updatePersonalInfo(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Personal information updated successfully"));
    }

    @PutMapping("/organization")
    @Operation(summary = "Update Organization Information", description = "Updates company details (Name, Industry, Type, Designation, PAN, CIN, Address, Website)")
    public ResponseEntity<ApiResponse<OrganizationInfoDto>> updateOrganizationInfo(@RequestBody OrganizationInfoDto update) {
        OrganizationInfoDto updated = profileSettingsService.updateOrganizationInfo(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Organization information updated successfully"));
    }

    @PutMapping("/contact")
    @Operation(summary = "Update Contact Information", description = "Updates contact information (Primary/Alternate Mobile, Alternate Email)")
    public ResponseEntity<ApiResponse<ContactInfoDto>> updateContactInfo(@RequestBody ContactInfoDto update) {
        ContactInfoDto updated = profileSettingsService.updateContactInfo(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Contact information updated successfully"));
    }

    @PutMapping("/professional")
    @Operation(summary = "Update Professional Details", description = "Updates professional details (Experience, Expertise, Interested Sectors, Investment Range & Purpose)")
    public ResponseEntity<ApiResponse<ProfessionalDetailsDto>> updateProfessionalDetails(@RequestBody ProfessionalDetailsDto update) {
        ProfessionalDetailsDto updated = profileSettingsService.updateProfessionalDetails(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Professional details updated successfully"));
    }

    @PutMapping("/address")
    @Operation(summary = "Update Address Information", description = "Updates residential / registered address")
    public ResponseEntity<ApiResponse<AddressInfoDto>> updateAddressInfo(@RequestBody AddressInfoDto update) {
        AddressInfoDto updated = profileSettingsService.updateAddressInfo(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Address updated successfully"));
    }

    @PutMapping("/additional")
    @Operation(summary = "Update Additional Information", description = "Updates language preferences, referral source, and profile visibility")
    public ResponseEntity<ApiResponse<AdditionalInfoDto>> updateAdditionalInfo(@RequestBody AdditionalInfoDto update) {
        AdditionalInfoDto updated = profileSettingsService.updateAdditionalInfo(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Additional information updated successfully"));
    }

    // =========================================================================
    // 2. KYC & Verification
    // =========================================================================

    @GetMapping("/kyc")
    @Operation(summary = "Get KYC & Verification Overview", description = "Retrieves KYC document checklist, 80% completion donut metrics, verification steps, and audit history")
    public ResponseEntity<ApiResponse<KycVerificationPageDto>> getKycVerification() {
        KycVerificationPageDto kyc = profileSettingsService.getKycVerification();
        return ResponseEntity.ok(ApiResponse.success(kyc, "KYC verification details retrieved successfully"));
    }

    @PostMapping("/kyc/documents/{docId}/upload")
    @Operation(summary = "Upload KYC Document", description = "Uploads a required document such as Self Declaration Form or Cancelled Cheque")
    public ResponseEntity<ApiResponse<KycDocumentItemDto>> uploadKycDocument(
            @PathVariable Long docId,
            @RequestBody(required = false) Map<String, String> payload
    ) {
        String fileUrl = (payload != null && payload.containsKey("fileUrl")) ? payload.get("fileUrl") : null;
        KycDocumentItemDto doc = profileSettingsService.uploadKycDocument(docId, fileUrl);
        return ResponseEntity.ok(ApiResponse.success(doc, "Document uploaded successfully and submitted for verification"));
    }

    // =========================================================================
    // 3. Bank & Settlement Accounts
    // =========================================================================

    @GetMapping("/bank-accounts")
    @Operation(summary = "Get Linked Bank Accounts", description = "Retrieves all linked bank accounts (SBI primary, HDFC, ICICI, Axis), account guidelines, and settlement info")
    public ResponseEntity<ApiResponse<BankAccountsPageDto>> getBankAccounts() {
        BankAccountsPageDto page = profileSettingsService.getBankAccountsPage();
        return ResponseEntity.ok(ApiResponse.success(page, "Bank accounts retrieved successfully"));
    }

    @PostMapping("/bank-accounts")
    @Operation(summary = "Add New Bank Account", description = "Links a new Indian bank account for trading settlements and payouts")
    public ResponseEntity<ApiResponse<BankAccountItemDto>> addBankAccount(@Valid @RequestBody AddBankAccountRequestDto input) {
        BankAccountItemDto added = profileSettingsService.addBankAccount(input);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(added, "Bank account added successfully"));
    }

    @PutMapping("/bank-accounts/{accountId}/primary")
    @Operation(summary = "Set Primary Bank Account", description = "Sets specified account as primary for all settlements, payouts and refunds")
    public ResponseEntity<ApiResponse<BankAccountItemDto>> setPrimaryAccount(@PathVariable Long accountId) {
        BankAccountItemDto updated = profileSettingsService.setPrimaryAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(updated, updated.getBankName() + " set as primary settlement account"));
    }

    @DeleteMapping("/bank-accounts/{accountId}")
    @Operation(summary = "Remove Bank Account", description = "Deletes a linked bank account")
    public ResponseEntity<ApiResponse<Void>> removeBankAccount(@PathVariable Long accountId) {
        profileSettingsService.removeBankAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success(null, "Bank account removed successfully"));
    }

    // =========================================================================
    // 4. Security & Login Activity
    // =========================================================================

    @GetMapping("/security")
    @Operation(summary = "Get Security Overview", description = "Retrieves password status, 2FA settings, active sessions/devices, login history, and security toggles")
    public ResponseEntity<ApiResponse<SecurityOverviewDto>> getSecurityOverview() {
        SecurityOverviewDto security = profileSettingsService.getSecurityOverview();
        return ResponseEntity.ok(ApiResponse.success(security, "Security overview retrieved successfully"));
    }

    @PostMapping("/security/change-password")
    @Operation(summary = "Change Password", description = "Updates account password with confirmation check")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDto input) {
        profileSettingsService.changePassword(input);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @PutMapping("/security/2fa")
    @Operation(summary = "Toggle Two-Factor Authentication", description = "Enables or disables 2FA protection")
    public ResponseEntity<ApiResponse<TwoFactorSettingsDto>> toggleTwoFactor(@RequestParam boolean enable) {
        TwoFactorSettingsDto updated = profileSettingsService.toggleTwoFactor(enable);
        return ResponseEntity.ok(ApiResponse.success(updated, "Two-factor authentication updated"));
    }

    @PutMapping("/security/preferences")
    @Operation(summary = "Update Security Preferences", description = "Updates security toggles (Email on login, Login alerts, Require 2FA, Auto logout)")
    public ResponseEntity<ApiResponse<SecurityPreferencesDto>> updateSecurityPreferences(@RequestBody SecurityPreferencesDto update) {
        SecurityPreferencesDto updated = profileSettingsService.updateSecurityPreferences(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Security preferences saved"));
    }

    @DeleteMapping("/security/sessions/{sessionId}")
    @Operation(summary = "Terminate Session", description = "Terminates a specific remote session/device")
    public ResponseEntity<ApiResponse<Void>> terminateSession(@PathVariable String sessionId) {
        profileSettingsService.terminateSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Session terminated successfully"));
    }

    @DeleteMapping("/security/sessions")
    @Operation(summary = "Terminate All Other Sessions", description = "Terminates all active sessions except the current session")
    public ResponseEntity<ApiResponse<Void>> terminateAllOtherSessions() {
        profileSettingsService.terminateAllOtherSessions();
        return ResponseEntity.ok(ApiResponse.success(null, "All other sessions terminated successfully"));
    }

    // =========================================================================
    // 5. Active Devices (Image 1)
    // =========================================================================

    @GetMapping("/devices")
    @Operation(summary = "Get Active Devices", description = "Retrieves currently logged in devices, device limits (2/5), security tips, and recent device activity (Image 1)")
    public ResponseEntity<ApiResponse<ActiveDevicesPageDto>> getActiveDevices() {
        ActiveDevicesPageDto page = profileSettingsService.getActiveDevicesPage();
        return ResponseEntity.ok(ApiResponse.success(page, "Active devices retrieved successfully"));
    }

    @DeleteMapping("/devices/{deviceId}")
    @Operation(summary = "Revoke Device Access", description = "Revokes access for a specific device (e.g. Android Chrome, Windows Edge, MacBook Chrome)")
    public ResponseEntity<ApiResponse<Void>> revokeDevice(@PathVariable String deviceId) {
        profileSettingsService.revokeDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(null, "Device access revoked successfully"));
    }

    // =========================================================================
    // 6. Notifications (Image 2)
    // =========================================================================

    @GetMapping("/notifications")
    @Operation(summary = "Get Notifications", description = "Retrieves all notifications, unread count, category filters, notification preferences, and channels (Image 2)")
    public ResponseEntity<ApiResponse<NotificationsPageDto>> getNotifications(
            @RequestParam(required = false, defaultValue = "ALL") String tab,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type
    ) {
        NotificationsPageDto page = profileSettingsService.getNotificationsPage(tab, search, type);
        return ResponseEntity.ok(ApiResponse.success(page, "Notifications retrieved successfully"));
    }

    @PutMapping("/notifications/read-all")
    @Operation(summary = "Mark All Notifications as Read", description = "Marks all unread notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        profileSettingsService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "Mark Notification as Read", description = "Marks a specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        profileSettingsService.markNotificationAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PutMapping("/notifications/preferences")
    @Operation(summary = "Update Notification Preferences", description = "Toggles preferences for Trade, Negotiation, Settlement, KYC, Member Requests, System, Security, Marketing")
    public ResponseEntity<ApiResponse<NotificationPreferencesDto>> updateNotificationPreferences(@RequestBody NotificationPreferencesDto update) {
        NotificationPreferencesDto updated = profileSettingsService.updateNotificationPreferences(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification preferences updated successfully"));
    }

    @PutMapping("/notifications/channels")
    @Operation(summary = "Update Notification Channels", description = "Toggles In-App, Email, and SMS notification delivery channels")
    public ResponseEntity<ApiResponse<NotificationChannelsDto>> updateNotificationChannels(@RequestBody NotificationChannelsDto update) {
        NotificationChannelsDto updated = profileSettingsService.updateNotificationChannels(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Notification channels updated successfully"));
    }

    // =========================================================================
    // 7. Documents (Image 3)
    // =========================================================================

    @GetMapping("/documents")
    @Operation(summary = "Get Stored Documents", description = "Retrieves all user documents, storage usage (40%, 2.0/5.0 MB), guidelines, and upcoming expiry alerts (Image 3)")
    public ResponseEntity<ApiResponse<DocumentsPageDto>> getDocuments(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search
    ) {
        DocumentsPageDto page = profileSettingsService.getDocumentsPage(category, search);
        return ResponseEntity.ok(ApiResponse.success(page, "Documents retrieved successfully"));
    }

    @PostMapping("/documents/upload")
    @Operation(summary = "Quick Upload Document", description = "Uploads a new document into storage")
    public ResponseEntity<ApiResponse<DocumentRecordDto>> uploadDocument(
            @RequestBody Map<String, String> payload
    ) {
        String name = payload.get("name");
        String category = payload.get("category");
        String fileName = payload.get("fileName");
        String fileUrl = payload.get("fileUrl");
        DocumentRecordDto doc = profileSettingsService.uploadDocument(name, category, fileName, fileUrl);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doc, "Document uploaded successfully"));
    }

    // =========================================================================
    // 8. Platform Settings (Image 4)
    // =========================================================================

    @GetMapping("/settings")
    @Operation(summary = "Get Platform Settings", description = "Retrieves General settings, Platform preferences, Appearance theme, Language & Region, Data export and actions (Image 4)")
    public ResponseEntity<ApiResponse<PlatformSettingsPageDto>> getPlatformSettings() {
        PlatformSettingsPageDto page = profileSettingsService.getPlatformSettingsPage();
        return ResponseEntity.ok(ApiResponse.success(page, "Platform settings retrieved successfully"));
    }

    @PutMapping("/settings/general")
    @Operation(summary = "Update General Settings", description = "Updates email, mobile, company, designation")
    public ResponseEntity<ApiResponse<GeneralSettingsDto>> updateGeneralSettings(@RequestBody GeneralSettingsDto update) {
        GeneralSettingsDto updated = profileSettingsService.updateGeneralSettings(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "General settings updated successfully"));
    }

    @PutMapping("/settings/preferences")
    @Operation(summary = "Update Platform Preferences", description = "Updates platform toggles (Email, SMS, Marketing, Show Portfolio Summary, Auto Logout)")
    public ResponseEntity<ApiResponse<PlatformPreferencesDto>> updatePlatformPreferences(@RequestBody PlatformPreferencesDto update) {
        PlatformPreferencesDto updated = profileSettingsService.updatePlatformPreferences(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Platform preferences updated successfully"));
    }

    @PutMapping("/settings/appearance")
    @Operation(summary = "Update Theme Appearance", description = "Selects Light, Dark, or System display theme")
    public ResponseEntity<ApiResponse<AppearanceSettingsDto>> updateAppearance(@RequestBody AppearanceSettingsDto update) {
        AppearanceSettingsDto updated = profileSettingsService.updateAppearance(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Appearance theme updated to " + updated.getSelectedTheme()));
    }

    @PutMapping("/settings/language-region")
    @Operation(summary = "Update Language & Region", description = "Updates preferred language and timezone")
    public ResponseEntity<ApiResponse<LanguageRegionDto>> updateLanguageRegion(@RequestBody LanguageRegionDto update) {
        LanguageRegionDto updated = profileSettingsService.updateLanguageRegion(update);
        return ResponseEntity.ok(ApiResponse.success(updated, "Language & Region settings updated"));
    }

    @GetMapping("/settings/export")
    @Operation(summary = "Export Account Data", description = "Exports profile, trades, portfolio and activity data")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportAccountData() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("downloadUrl", "https://atirathgroup.com/downloads/account-data-ATG10027.json", "status", "READY"),
                "Account data export generated successfully"
        ));
    }

    @GetMapping("/settings/download-statements")
    @Operation(summary = "Download Trade Statements", description = "Downloads trade statements and settlement reports")
    public ResponseEntity<ApiResponse<Map<String, String>>> downloadStatements() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("downloadUrl", "https://atirathgroup.com/downloads/trade-statements-ATG10027.pdf", "status", "READY"),
                "Trade statements generated successfully"
        ));
    }
}
