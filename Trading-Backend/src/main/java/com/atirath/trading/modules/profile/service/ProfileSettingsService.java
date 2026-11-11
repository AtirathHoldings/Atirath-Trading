package com.atirath.trading.modules.profile.service;

import com.atirath.trading.modules.profile.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProfileSettingsService {

    private MemberProfileDto memberProfile;
    private KycVerificationPageDto kycVerification;
    private final List<BankAccountItemDto> bankAccounts = new CopyOnWriteArrayList<>();
    private SecurityOverviewDto securityOverview;

    // --- New Modules Data Stores ---
    private final List<DeviceItemDto> devices = new CopyOnWriteArrayList<>();
    private final List<NotificationItemDto> notifications = new CopyOnWriteArrayList<>();
    private NotificationPreferencesDto notificationPreferences;
    private NotificationChannelsDto notificationChannels;
    private final List<DocumentRecordDto> documents = new CopyOnWriteArrayList<>();
    private StorageUsageDto storageUsage;
    private final List<ExpiryAlertDto> expiryAlerts = new CopyOnWriteArrayList<>();
    private GeneralSettingsDto generalSettings;
    private PlatformPreferencesDto platformPreferences;
    private AppearanceSettingsDto appearanceSettings;
    private LanguageRegionDto languageRegion;

    private final AtomicLong bankIdCounter = new AtomicLong(10);
    private final AtomicLong kycDocCounter = new AtomicLong(10);
    private final AtomicLong docCounter = new AtomicLong(20);

    private static final DateTimeFormatter DISPLAY_DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @PostConstruct
    public void init() {
        seedProfileData();
        seedKycData();
        seedBankData();
        seedSecurityData();
        seedDevicesData();
        seedNotificationsData();
        seedDocumentsData();
        seedSettingsData();
        log.info("ProfileSettingsService initialized with in-memory data for Rohit Sharma (ATG10027)");
    }

    private void seedProfileData() {
        PersonalInfoDto personal = PersonalInfoDto.builder()
                .fullName("Rohit Sharma")
                .dateOfBirth("12 Mar 1995")
                .gender("Male")
                .nationality("Indian")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("1234 5678 9012")
                .aadhaarStatus("Verified")
                .occupation("Business Professional")
                .linkedInProfile("https://linkedin.com/in/rohitsharma")
                .build();

        OrganizationInfoDto org = OrganizationInfoDto.builder()
                .companyName("GreenTech Solutions Pvt Ltd")
                .industry("Renewable Energy")
                .companyType("Private Limited")
                .designation("Director")
                .companyPan("AABCG1234H")
                .registrationNumber("U40100TG2020PTC123456")
                .companyAddress("Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
                .website("www.greentechsolutions.com")
                .logoUrl("GreenTech Solutions Renewable Energy for a Better Tomorrow")
                .build();

        ContactInfoDto contact = ContactInfoDto.builder()
                .emailAddress("rohit.sharma@example.com")
                .emailStatus("Verified")
                .mobileNumber("+91 98765 43210")
                .mobileStatus("Verified")
                .alternateMobile("+91 91234 56789")
                .alternateEmail("rohit.alt@example.com")
                .build();

        ProfessionalDetailsDto prof = ProfessionalDetailsDto.builder()
                .yearsOfExperience("8+ Years")
                .areaOfExpertise("Renewable Energy, Project Development")
                .interestedSectors(new ArrayList<>(Arrays.asList("Renewable Energy", "Agriculture", "Infrastructure")))
                .investmentRange("₹10,00,000 - ₹50,00,000")
                .investmentPurpose("Strategic Investment")
                .build();

        AddressInfoDto addr = AddressInfoDto.builder()
                .currentAddress("Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081 India")
                .build();

        AdditionalInfoDto addl = AdditionalInfoDto.builder()
                .languageKnown(new ArrayList<>(Arrays.asList("English", "Hindi", "Telugu")))
                .howDidYouHearAboutUs("Referral")
                .profileVisibility("Visible to verified members")
                .build();

        memberProfile = MemberProfileDto.builder()
                .memberId("ATG10027")
                .fullName("Rohit Sharma")
                .memberBadge("Verified Member")
                .email("rohit.sharma@example.com")
                .mobileNumber("+91 98765 43210")
                .companyName("GreenTech Solutions Pvt Ltd")
                .designation("Director")
                .industry("Renewable Energy")
                .memberSince("12 Jan 2021")
                .profileCompletion(90)
                .avatarInitials("RS")
                .personalInfo(personal)
                .organizationInfo(org)
                .contactInfo(contact)
                .professionalDetails(prof)
                .addressInfo(addr)
                .additionalInfo(addl)
                .build();
    }

    private void seedKycData() {
        List<KycDocumentItemDto> docs = new ArrayList<>();
        docs.add(KycDocumentItemDto.builder()
                .id(1L)
                .title("Identity Proof (PAN)")
                .description("Upload your PAN card for identity verification.")
                .documentType("PAN Card")
                .documentNumber("ABCDE1234F")
                .uploadedOn("15 Sep 2026")
                .status("Verified")
                .fileUrl("https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=600&q=80")
                .canUpload(false)
                .build());

        docs.add(KycDocumentItemDto.builder()
                .id(2L)
                .title("Address Proof")
                .description("Upload address proof (Aadhaar, Passport or Voter ID).")
                .documentType("Aadhaar Card")
                .documentNumber("1234 5678 9012")
                .uploadedOn("15 Sep 2026")
                .status("Verified")
                .fileUrl("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80")
                .canUpload(false)
                .build());

        docs.add(KycDocumentItemDto.builder()
                .id(3L)
                .title("Organization Documents")
                .description("Upload company registration and related documents.")
                .documentType("Certificate of Incorporation")
                .documentNumber("U40100TG2020PTC123456")
                .uploadedOn("12 Jan 2020")
                .status("Verified")
                .fileUrl("https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=600&q=80")
                .canUpload(false)
                .build());

        docs.add(KycDocumentItemDto.builder()
                .id(4L)
                .title("Bank Details")
                .description("Upload bank account proof (canceled cheque or bank statement).")
                .documentType("Cancelled Cheque")
                .documentNumber("SBIN0001234")
                .uploadedOn("16 Sep 2026")
                .status("Under Review")
                .fileUrl("https://images.unsplash.com/photo-1568602471122-7832951cc4c5?auto=format&fit=crop&w=600&q=80")
                .canUpload(false)
                .build());

        docs.add(KycDocumentItemDto.builder()
                .id(5L)
                .title("Self Declaration")
                .description("Upload signed self declaration form (as per compliance).")
                .documentType("Self Declaration Form")
                .documentNumber("-")
                .uploadedOn("-")
                .status("Not Verified")
                .canUpload(true)
                .build());

        List<VerificationHistoryLogDto> hist = new ArrayList<>();
        hist.add(VerificationHistoryLogDto.builder().title("PAN Verified").timestamp("15 Sep 2026, 02:15 PM").actor("Anitha Verma (Compliance)").status("Verified").build());
        hist.add(VerificationHistoryLogDto.builder().title("Address Verified").timestamp("15 Sep 2026, 02:40 PM").actor("Anitha Verma (Compliance)").status("Verified").build());
        hist.add(VerificationHistoryLogDto.builder().title("Organization Verified").timestamp("16 Sep 2026, 10:20 AM").actor("Suresh Kumar (Operations)").status("Verified").build());
        hist.add(VerificationHistoryLogDto.builder().title("Bank Document Submitted").timestamp("16 Sep 2026, 11:30 AM").actor("Rohit Sharma").status("Submitted").build());
        hist.add(VerificationHistoryLogDto.builder().title("Self Declaration Pending").timestamp("-").actor("-").status("Pending").build());

        kycVerification = KycVerificationPageDto.builder()
                .memberId("ATG10027")
                .fullName("Rohit Sharma")
                .memberBadge("Verified Member")
                .email("rohit.sharma@example.com")
                .mobileNumber("+91 98765 43210")
                .companyName("GreenTech Solutions Pvt Ltd")
                .designation("Director")
                .industry("Renewable Energy")
                .memberSince("12 Jan 2021")
                .avatarInitials("RS")
                .activeStep(4)
                .stepperSteps(new ArrayList<>(Arrays.asList("Identity Verification", "Address Verification", "Organization Verification", "Bank Verification", "Final Review")))
                .completionPercentage(80)
                .verifiedCount(4)
                .underReviewCount(1)
                .notVerifiedCount(1)
                .notRequiredCount(0)
                .statusAlertTitle("Identity, Address and Organization verification completed.")
                .statusAlertMessage("Complete the remaining verification to access all platform features.")
                .importantNotes(new ArrayList<>(Arrays.asList(
                        "All documents must be clear and valid.",
                        "Accepted formats: PDF, JPG, PNG (Max 5 MB).",
                        "Ensure document details match your profile information.",
                        "Verification may take 1-2 business days."
                )))
                .documents(docs)
                .verificationHistory(hist)
                .build();
    }

    private void seedBankData() {
        bankAccounts.add(BankAccountItemDto.builder()
                .id(1L)
                .bankName("State Bank of India")
                .bankLogo("SBI")
                .accountHolderName("Rohit Sharma")
                .accountNumber("1234 5678 9012")
                .ifscCode("SBIN0001234")
                .isPrimary(true)
                .status("Verified")
                .addedOn("12 Jan 2024")
                .build());

        bankAccounts.add(BankAccountItemDto.builder()
                .id(2L)
                .bankName("HDFC Bank")
                .bankLogo("HDFC")
                .accountHolderName("Rohit Sharma")
                .accountNumber("9876 5432 1012")
                .ifscCode("HDFC0002345")
                .isPrimary(false)
                .status("Verified")
                .addedOn("05 Mar 2024")
                .build());

        bankAccounts.add(BankAccountItemDto.builder()
                .id(3L)
                .bankName("ICICI Bank")
                .bankLogo("ICICI")
                .accountHolderName("Rohit Sharma")
                .accountNumber("4567 8901 2345")
                .ifscCode("ICIC0003456")
                .isPrimary(false)
                .status("Verified")
                .addedOn("20 Jun 2024")
                .build());

        bankAccounts.add(BankAccountItemDto.builder()
                .id(4L)
                .bankName("Axis Bank")
                .bankLogo("AXIS")
                .accountHolderName("Rohit Sharma")
                .accountNumber("2345 6789 0123")
                .ifscCode("UTIB0004567")
                .isPrimary(false)
                .status("Under Verification")
                .addedOn("16 Sep 2026")
                .build());
    }

    private void seedSecurityData() {
        TwoFactorSettingsDto twoFactor = TwoFactorSettingsDto.builder()
                .isEnabled(true)
                .authenticatorAppStatus("Connected")
                .authenticatorAppEnabled(true)
                .smsPhoneNumber("+91 98765 43210")
                .smsVerificationEnabled(false)
                .backupCodesCount(8)
                .build();

        List<ActiveSessionDto> sessions = new ArrayList<>();
        sessions.add(ActiveSessionDto.builder().sessionId("sess-1").device("Windows · Chrome").location("Hyderabad, India").ipAddress("103.21.45.67").isCurrentSession(true).build());
        sessions.add(ActiveSessionDto.builder().sessionId("sess-2").device("Android · Chrome").location("Hyderabad, India").ipAddress("103.21.45.67").isCurrentSession(false).build());
        sessions.add(ActiveSessionDto.builder().sessionId("sess-3").device("iPhone · Safari").location("Bangalore, India").ipAddress("49.36.12.89").isCurrentSession(false).build());

        SecurityPreferencesDto prefs = SecurityPreferencesDto.builder()
                .emailNotificationsForNewLogin(true)
                .loginAlertsForUnknownDevices(true)
                .require2faForAllLogins(true)
                .autoLogoutForInactiveSessions(false)
                .build();

        List<LoginHistoryItemDto> history = new ArrayList<>();
        history.add(LoginHistoryItemDto.builder().dateTime("16 Sep 2026, 10:24 AM").device("Windows · Chrome").location("Hyderabad, India").ipAddress("103.21.45.67").status("Successful").build());
        history.add(LoginHistoryItemDto.builder().dateTime("15 Sep 2026, 08:17 PM").device("Android · Chrome").location("Hyderabad, India").ipAddress("103.21.45.67").status("Successful").build());
        history.add(LoginHistoryItemDto.builder().dateTime("14 Sep 2026, 11:05 AM").device("Windows · Edge").location("Bangalore, India").ipAddress("49.36.12.89").status("Successful").build());
        history.add(LoginHistoryItemDto.builder().dateTime("12 Sep 2026, 09:41 PM").device("iPhone · Safari").location("Hyderabad, India").ipAddress("103.21.45.67").status("Unsuccessful").build());

        securityOverview = SecurityOverviewDto.builder()
                .memberId("ATG10027")
                .fullName("Rohit Sharma")
                .memberBadge("Verified Member")
                .email("rohit.sharma@example.com")
                .mobileNumber("+91 98765 43210")
                .companyName("GreenTech Solutions Pvt Ltd")
                .designation("Director")
                .industry("Renewable Energy")
                .memberSince("12 Jan 2021")
                .avatarInitials("RS")
                .maskedPassword("••••••••••••")
                .passwordLastChanged("12 Jan 2024")
                .twoFactor(twoFactor)
                .activeSessions(sessions)
                .securityPreferences(prefs)
                .loginHistory(history)
                .build();
    }

    private void seedDevicesData() {
        devices.add(DeviceItemDto.builder()
                .id("dev-1")
                .name("Windows - Chrome")
                .deviceType("LAPTOP")
                .osBrowser("Windows 11 | Chrome 128.0.6613.137")
                .location("Hyderabad, India")
                .ipAddress("103.21.45.67")
                .status("Active Now")
                .isCurrentDevice(true)
                .canRevoke(false)
                .build());

        devices.add(DeviceItemDto.builder()
                .id("dev-2")
                .name("iPhone - Safari")
                .deviceType("MOBILE")
                .osBrowser("iOS 17.6 | Safari 17.6")
                .location("Bangalore, India")
                .ipAddress("49.36.12.89")
                .status("Active")
                .isCurrentDevice(false)
                .lastActive("15 Sep 2026, 06:24 PM")
                .canRevoke(false)
                .build());

        devices.add(DeviceItemDto.builder()
                .id("dev-3")
                .name("Android - Chrome")
                .deviceType("MOBILE")
                .osBrowser("Android 14 | Chrome 127.0.6533.103")
                .location("Hyderabad, India")
                .ipAddress("103.21.45.67")
                .status("Inactive")
                .isCurrentDevice(false)
                .lastActive("12 Sep 2026, 11:18 AM")
                .canRevoke(true)
                .build());

        devices.add(DeviceItemDto.builder()
                .id("dev-4")
                .name("Windows - Edge")
                .deviceType("DESKTOP")
                .osBrowser("Windows 10 | Edge 127.0.2651.86")
                .location("Hyderabad, India")
                .ipAddress("203.122.44.11")
                .status("Inactive")
                .isCurrentDevice(false)
                .lastActive("05 Sep 2026, 09:41 AM")
                .canRevoke(true)
                .build());

        devices.add(DeviceItemDto.builder()
                .id("dev-5")
                .name("MacBook - Chrome")
                .deviceType("LAPTOP")
                .osBrowser("macOS 14.5 | Chrome 126.0.6478.126")
                .location("Chennai, India")
                .ipAddress("49.204.18.73")
                .status("Inactive")
                .isCurrentDevice(false)
                .lastActive("28 Aug 2026, 03:27 PM")
                .canRevoke(true)
                .build());
    }

    private void seedNotificationsData() {
        notifications.add(NotificationItemDto.builder()
                .id("notif-1")
                .title("Trade Order Approved")
                .message("Your buy order #TRD00234 for Solar Panels has been approved.")
                .type("TRADE")
                .timeAgo("10 mins ago")
                .isUnread(true)
                .iconTheme("green")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-2")
                .title("New Negotiation Message")
                .message("You have received a new message from Global Renewables Ltd.")
                .type("NEGOTIATION")
                .timeAgo("2 hours ago")
                .isUnread(true)
                .iconTheme("purple")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-3")
                .title("Settlement Completed")
                .message("Settlement for trade #TRD00212 has been completed successfully.")
                .type("SETTLEMENT")
                .timeAgo("4 hours ago")
                .isUnread(false)
                .iconTheme("orange")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-4")
                .title("KYC Verification Update")
                .message("Your KYC documents have been verified successfully.")
                .type("KYC")
                .timeAgo("1 day ago")
                .isUnread(false)
                .iconTheme("red")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-5")
                .title("New Member Request")
                .message("A new external member request is pending your review.")
                .type("MEMBER_REQUEST")
                .timeAgo("1 day ago")
                .isUnread(false)
                .iconTheme("blue")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-6")
                .title("System Update")
                .message("New features have been added to the platform. Check what's new!")
                .type("SYSTEM")
                .timeAgo("3 days ago")
                .isUnread(false)
                .iconTheme("dark")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-7")
                .title("Document Expiry Reminder")
                .message("Your PAN card will expire in 30 days. Please update the document.")
                .type("REMINDER")
                .timeAgo("5 days ago")
                .isUnread(false)
                .iconTheme("green")
                .build());

        notifications.add(NotificationItemDto.builder()
                .id("notif-8")
                .title("New Login Detected")
                .message("A new login was detected from Windows - Hyderabad, India.")
                .type("SECURITY")
                .timeAgo("6 days ago")
                .isUnread(false)
                .iconTheme("orange")
                .build());

        notificationPreferences = NotificationPreferencesDto.builder()
                .tradeNotifications(true)
                .negotiationMessages(true)
                .settlementUpdates(true)
                .kycCompliance(true)
                .memberRequests(true)
                .systemUpdates(true)
                .securityAlerts(true)
                .marketingCommunications(false)
                .build();

        notificationChannels = NotificationChannelsDto.builder()
                .inApp(true)
                .email(true)
                .sms(false)
                .build();
    }

    private void seedDocumentsData() {
        documents.add(DocumentRecordDto.builder().id("doc-1").name("PAN Card").fileName("pan_card.pdf").category("Identity").uploadedOn("12 Jan 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-2").name("Aadhaar Card").fileName("aadhaar_card.pdf").category("Identity").uploadedOn("12 Jan 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-3").name("Voter ID").fileName("voter_id.pdf").category("Identity").uploadedOn("15 Feb 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-4").name("Certificate of Incorporation").fileName("coi.pdf").category("Organization").uploadedOn("05 Mar 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-5").name("MoA & AoA").fileName("moa_aoa.pdf").category("Organization").uploadedOn("05 Mar 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-6").name("GST Registration Certificate").fileName("gst_certificate.pdf").category("Organization").uploadedOn("10 Mar 2024").expiryDate("10 Mar 2029").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-7").name("Current Account Proof").fileName("bank_statement.pdf").category("Bank").uploadedOn("20 Mar 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-8").name("Cancelled Cheque").fileName("cancelled_cheque.pdf").category("Bank").uploadedOn("20 Mar 2024").expiryDate("-").status("Verified").build());
        documents.add(DocumentRecordDto.builder().id("doc-9").name("Board Resolution").fileName("board_resolution.pdf").category("Other").uploadedOn("15 Apr 2024").expiryDate("-").status("Under Review").build());
        documents.add(DocumentRecordDto.builder().id("doc-10").name("Trading Authorization").fileName("trading_authorization.pdf").category("Other").uploadedOn("15 Apr 2024").expiryDate("15 Apr 2025").status("Expired").build());

        storageUsage = StorageUsageDto.builder()
                .usedPercentage(40)
                .usedMb("2.0 MB")
                .totalMb("5.0 MB")
                .availableMb("3.0 MB")
                .build();

        expiryAlerts.add(ExpiryAlertDto.builder().name("Trading Authorization").alertText("Expires in 30 days").expiryDate("15 Apr 2025").theme("red").build());
        expiryAlerts.add(ExpiryAlertDto.builder().name("GST Registration Certificate").alertText("Expires in 180 days").expiryDate("10 Mar 2029").theme("green").build());
        expiryAlerts.add(ExpiryAlertDto.builder().name("PAN Card").alertText("No expiry").expiryDate("-").theme("orange").build());
    }

    private void seedSettingsData() {
        generalSettings = GeneralSettingsDto.builder()
                .emailAddress("rohit.sharma@example.com")
                .emailStatus("Verified")
                .mobileNumber("+91 98765 43210")
                .mobileStatus("Verified")
                .company("GreenTech Solutions Pvt Ltd")
                .designation("Director")
                .build();

        platformPreferences = PlatformPreferencesDto.builder()
                .receiveEmailNotifications(true)
                .receiveSmsNotifications(true)
                .marketingCommunications(false)
                .showPortfolioSummaryOnLogin(true)
                .autoLogout("30 Minutes")
                .build();

        appearanceSettings = AppearanceSettingsDto.builder()
                .selectedTheme("Light")
                .build();

        languageRegion = LanguageRegionDto.builder()
                .language("English (India)")
                .timezone("(UTC+05:30) Asia/Kolkata")
                .build();
    }

    // --- Profile Screen APIs ---
    public MemberProfileDto getProfile() {
        return memberProfile;
    }

    public PersonalInfoDto updatePersonalInfo(PersonalInfoDto update) {
        if (update.getFullName() != null) memberProfile.getPersonalInfo().setFullName(update.getFullName());
        if (update.getDateOfBirth() != null) memberProfile.getPersonalInfo().setDateOfBirth(update.getDateOfBirth());
        if (update.getGender() != null) memberProfile.getPersonalInfo().setGender(update.getGender());
        if (update.getNationality() != null) memberProfile.getPersonalInfo().setNationality(update.getNationality());
        if (update.getPanNumber() != null) memberProfile.getPersonalInfo().setPanNumber(update.getPanNumber());
        if (update.getAadhaarNumber() != null) memberProfile.getPersonalInfo().setAadhaarNumber(update.getAadhaarNumber());
        if (update.getOccupation() != null) memberProfile.getPersonalInfo().setOccupation(update.getOccupation());
        if (update.getLinkedInProfile() != null) memberProfile.getPersonalInfo().setLinkedInProfile(update.getLinkedInProfile());
        return memberProfile.getPersonalInfo();
    }

    public OrganizationInfoDto updateOrganizationInfo(OrganizationInfoDto update) {
        if (update.getCompanyName() != null) memberProfile.getOrganizationInfo().setCompanyName(update.getCompanyName());
        if (update.getIndustry() != null) memberProfile.getOrganizationInfo().setIndustry(update.getIndustry());
        if (update.getCompanyType() != null) memberProfile.getOrganizationInfo().setCompanyType(update.getCompanyType());
        if (update.getDesignation() != null) memberProfile.getOrganizationInfo().setDesignation(update.getDesignation());
        if (update.getCompanyPan() != null) memberProfile.getOrganizationInfo().setCompanyPan(update.getCompanyPan());
        if (update.getRegistrationNumber() != null) memberProfile.getOrganizationInfo().setRegistrationNumber(update.getRegistrationNumber());
        if (update.getCompanyAddress() != null) memberProfile.getOrganizationInfo().setCompanyAddress(update.getCompanyAddress());
        if (update.getWebsite() != null) memberProfile.getOrganizationInfo().setWebsite(update.getWebsite());
        return memberProfile.getOrganizationInfo();
    }

    public ContactInfoDto updateContactInfo(ContactInfoDto update) {
        if (update.getEmailAddress() != null) memberProfile.getContactInfo().setEmailAddress(update.getEmailAddress());
        if (update.getMobileNumber() != null) memberProfile.getContactInfo().setMobileNumber(update.getMobileNumber());
        if (update.getAlternateMobile() != null) memberProfile.getContactInfo().setAlternateMobile(update.getAlternateMobile());
        if (update.getAlternateEmail() != null) memberProfile.getContactInfo().setAlternateEmail(update.getAlternateEmail());
        return memberProfile.getContactInfo();
    }

    public ProfessionalDetailsDto updateProfessionalDetails(ProfessionalDetailsDto update) {
        if (update.getYearsOfExperience() != null) memberProfile.getProfessionalDetails().setYearsOfExperience(update.getYearsOfExperience());
        if (update.getAreaOfExpertise() != null) memberProfile.getProfessionalDetails().setAreaOfExpertise(update.getAreaOfExpertise());
        if (update.getInterestedSectors() != null) memberProfile.getProfessionalDetails().setInterestedSectors(update.getInterestedSectors());
        if (update.getInvestmentRange() != null) memberProfile.getProfessionalDetails().setInvestmentRange(update.getInvestmentRange());
        if (update.getInvestmentPurpose() != null) memberProfile.getProfessionalDetails().setInvestmentPurpose(update.getInvestmentPurpose());
        return memberProfile.getProfessionalDetails();
    }

    public AddressInfoDto updateAddressInfo(AddressInfoDto update) {
        if (update.getCurrentAddress() != null) memberProfile.getAddressInfo().setCurrentAddress(update.getCurrentAddress());
        return memberProfile.getAddressInfo();
    }

    public AdditionalInfoDto updateAdditionalInfo(AdditionalInfoDto update) {
        if (update.getLanguageKnown() != null) memberProfile.getAdditionalInfo().setLanguageKnown(update.getLanguageKnown());
        if (update.getHowDidYouHearAboutUs() != null) memberProfile.getAdditionalInfo().setHowDidYouHearAboutUs(update.getHowDidYouHearAboutUs());
        if (update.getProfileVisibility() != null) memberProfile.getAdditionalInfo().setProfileVisibility(update.getProfileVisibility());
        return memberProfile.getAdditionalInfo();
    }

    // --- KYC Screen APIs ---
    public KycVerificationPageDto getKycVerification() {
        return kycVerification;
    }

    public KycDocumentItemDto uploadKycDocument(Long docId, String fileUrl) {
        KycDocumentItemDto doc = kycVerification.getDocuments().stream()
                .filter(d -> d.getId().equals(docId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID " + docId));

        doc.setStatus("Under Review");
        doc.setUploadedOn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        doc.setFileUrl(fileUrl != null ? fileUrl : "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=600&q=80");
        doc.setCanUpload(false);

        kycVerification.getVerificationHistory().add(0, VerificationHistoryLogDto.builder()
                .title(doc.getTitle() + " Submitted")
                .timestamp(LocalDateTime.now().format(DISPLAY_DT_FMT))
                .actor(memberProfile.getFullName())
                .status("Submitted")
                .build());

        return doc;
    }

    // --- Bank Accounts Screen APIs ---
    public BankAccountsPageDto getBankAccountsPage() {
        List<String> guidelines = Arrays.asList(
                "Bank account must be in your name or registered organization name.",
                "Only Indian bank accounts are supported.",
                "Account must be verified to receive settlements.",
                "Ensure correct IFSC and account details.",
                "You can add multiple bank accounts and set one as primary."
        );

        return BankAccountsPageDto.builder()
                .memberId(memberProfile.getMemberId())
                .fullName(memberProfile.getFullName())
                .memberBadge(memberProfile.getMemberBadge())
                .email(memberProfile.getEmail())
                .mobileNumber(memberProfile.getMobileNumber())
                .companyName(memberProfile.getCompanyName())
                .designation(memberProfile.getDesignation())
                .industry(memberProfile.getIndustry())
                .memberSince(memberProfile.getMemberSince())
                .avatarInitials(memberProfile.getAvatarInitials())
                .bankAccounts(new ArrayList<>(bankAccounts))
                .accountGuidelines(guidelines)
                .settlementInfo("Your primary bank account will be used for all settlements, payouts and refunds across the platform.")
                .build();
    }

    public BankAccountItemDto addBankAccount(AddBankAccountRequestDto input) {
        boolean isPrimary = Boolean.TRUE.equals(input.getSetAsPrimary()) || bankAccounts.isEmpty();
        if (isPrimary) {
            bankAccounts.forEach(b -> b.setIsPrimary(false));
        }

        String maskedAcc = input.getAccountNumber();
        if (maskedAcc.length() > 4) {
            maskedAcc = "**** **** " + maskedAcc.substring(maskedAcc.length() - 4);
        }

        BankAccountItemDto newAcc = BankAccountItemDto.builder()
                .id(bankIdCounter.incrementAndGet())
                .bankName(input.getBankName())
                .bankLogo(input.getBankName().split(" ")[0].toUpperCase())
                .accountHolderName(input.getAccountHolderName())
                .accountNumber(maskedAcc)
                .ifscCode(input.getIfscCode())
                .isPrimary(isPrimary)
                .status("Under Verification")
                .addedOn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();

        bankAccounts.add(newAcc);
        return newAcc;
    }

    public BankAccountItemDto setPrimaryAccount(Long accountId) {
        BankAccountItemDto target = bankAccounts.stream()
                .filter(b -> b.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bank account not found: " + accountId));

        bankAccounts.forEach(b -> b.setIsPrimary(false));
        target.setIsPrimary(true);
        return target;
    }

    public void removeBankAccount(Long accountId) {
        bankAccounts.removeIf(b -> b.getId().equals(accountId));
    }

    // --- Security Screen APIs ---
    public SecurityOverviewDto getSecurityOverview() {
        return securityOverview;
    }

    public boolean changePassword(ChangePasswordRequestDto input) {
        if (!input.getNewPassword().equals(input.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }
        securityOverview.setPasswordLastChanged(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        return true;
    }

    public TwoFactorSettingsDto toggleTwoFactor(boolean enable) {
        securityOverview.getTwoFactor().setIsEnabled(enable);
        return securityOverview.getTwoFactor();
    }

    public SecurityPreferencesDto updateSecurityPreferences(SecurityPreferencesDto update) {
        if (update.getEmailNotificationsForNewLogin() != null)
            securityOverview.getSecurityPreferences().setEmailNotificationsForNewLogin(update.getEmailNotificationsForNewLogin());
        if (update.getLoginAlertsForUnknownDevices() != null)
            securityOverview.getSecurityPreferences().setLoginAlertsForUnknownDevices(update.getLoginAlertsForUnknownDevices());
        if (update.getRequire2faForAllLogins() != null)
            securityOverview.getSecurityPreferences().setRequire2faForAllLogins(update.getRequire2faForAllLogins());
        if (update.getAutoLogoutForInactiveSessions() != null)
            securityOverview.getSecurityPreferences().setAutoLogoutForInactiveSessions(update.getAutoLogoutForInactiveSessions());
        return securityOverview.getSecurityPreferences();
    }

    public void terminateSession(String sessionId) {
        securityOverview.getActiveSessions().removeIf(s -> s.getSessionId().equals(sessionId) && !Boolean.TRUE.equals(s.getIsCurrentSession()));
    }

    public void terminateAllOtherSessions() {
        securityOverview.getActiveSessions().removeIf(s -> !Boolean.TRUE.equals(s.getIsCurrentSession()));
    }

    // =========================================================================
    // 5. Active Devices APIs (Image 1)
    // =========================================================================

    public ActiveDevicesPageDto getActiveDevicesPage() {
        int activeCount = (int) devices.stream().filter(d -> !"Inactive".equalsIgnoreCase(d.getStatus())).count();

        List<String> tips = Arrays.asList(
                "Regularly review your active devices.",
                "Revoke access for devices you no longer use.",
                "If you notice any suspicious activity, change your password immediately.",
                "Do not share your login credentials with anyone."
        );

        return ActiveDevicesPageDto.builder()
                .memberId(memberProfile.getMemberId())
                .fullName(memberProfile.getFullName())
                .memberBadge(memberProfile.getMemberBadge())
                .email(memberProfile.getEmail())
                .mobileNumber(memberProfile.getMobileNumber())
                .companyName(memberProfile.getCompanyName())
                .designation(memberProfile.getDesignation())
                .industry(memberProfile.getIndustry())
                .memberSince(memberProfile.getMemberSince())
                .avatarInitials(memberProfile.getAvatarInitials())
                .devices(new ArrayList<>(devices))
                .activeDevicesCount(activeCount)
                .maxDevicesLimit(5)
                .securityTips(tips)
                .recentActivity(securityOverview.getLoginHistory())
                .build();
    }

    public void revokeDevice(String deviceId) {
        devices.removeIf(d -> d.getId().equals(deviceId) && !Boolean.TRUE.equals(d.getIsCurrentDevice()));
    }

    // =========================================================================
    // 6. Notifications Screen APIs (Image 2)
    // =========================================================================

    public NotificationsPageDto getNotificationsPage(String tab, String search, String type) {
        List<NotificationItemDto> filtered = notifications.stream()
                .filter(n -> {
                    if ("UNREAD".equalsIgnoreCase(tab)) {
                        return Boolean.TRUE.equals(n.getIsUnread());
                    }
                    if ("MENTIONS".equalsIgnoreCase(tab)) {
                        return "NEGOTIATION".equalsIgnoreCase(n.getType());
                    }
                    if ("SYSTEM_UPDATES".equalsIgnoreCase(tab)) {
                        return "SYSTEM".equalsIgnoreCase(n.getType());
                    }
                    return true;
                })
                .filter(n -> {
                    if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) {
                        return type.equalsIgnoreCase(n.getType());
                    }
                    return true;
                })
                .filter(n -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String q = search.toLowerCase();
                        return n.getTitle().toLowerCase().contains(q) || n.getMessage().toLowerCase().contains(q);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int unread = (int) notifications.stream().filter(n -> Boolean.TRUE.equals(n.getIsUnread())).count();

        return NotificationsPageDto.builder()
                .memberId(memberProfile.getMemberId())
                .fullName(memberProfile.getFullName())
                .memberBadge(memberProfile.getMemberBadge())
                .email(memberProfile.getEmail())
                .mobileNumber(memberProfile.getMobileNumber())
                .companyName(memberProfile.getCompanyName())
                .designation(memberProfile.getDesignation())
                .industry(memberProfile.getIndustry())
                .memberSince(memberProfile.getMemberSince())
                .avatarInitials(memberProfile.getAvatarInitials())
                .unreadCount(unread)
                .notifications(filtered)
                .preferences(notificationPreferences)
                .channels(notificationChannels)
                .build();
    }

    public void markAllNotificationsAsRead() {
        notifications.forEach(n -> n.setIsUnread(false));
    }

    public void markNotificationAsRead(String id) {
        notifications.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .ifPresent(n -> n.setIsUnread(false));
    }

    public NotificationPreferencesDto updateNotificationPreferences(NotificationPreferencesDto update) {
        if (update.getTradeNotifications() != null) notificationPreferences.setTradeNotifications(update.getTradeNotifications());
        if (update.getNegotiationMessages() != null) notificationPreferences.setNegotiationMessages(update.getNegotiationMessages());
        if (update.getSettlementUpdates() != null) notificationPreferences.setSettlementUpdates(update.getSettlementUpdates());
        if (update.getKycCompliance() != null) notificationPreferences.setKycCompliance(update.getKycCompliance());
        if (update.getMemberRequests() != null) notificationPreferences.setMemberRequests(update.getMemberRequests());
        if (update.getSystemUpdates() != null) notificationPreferences.setSystemUpdates(update.getSystemUpdates());
        if (update.getSecurityAlerts() != null) notificationPreferences.setSecurityAlerts(update.getSecurityAlerts());
        if (update.getMarketingCommunications() != null) notificationPreferences.setMarketingCommunications(update.getMarketingCommunications());
        return notificationPreferences;
    }

    public NotificationChannelsDto updateNotificationChannels(NotificationChannelsDto update) {
        if (update.getInApp() != null) notificationChannels.setInApp(update.getInApp());
        if (update.getEmail() != null) notificationChannels.setEmail(update.getEmail());
        if (update.getSms() != null) notificationChannels.setSms(update.getSms());
        return notificationChannels;
    }

    // =========================================================================
    // 7. Documents Screen APIs (Image 3)
    // =========================================================================

    public DocumentsPageDto getDocumentsPage(String category, String search) {
        List<DocumentRecordDto> filtered = documents.stream()
                .filter(d -> {
                    if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
                        return category.equalsIgnoreCase(d.getCategory());
                    }
                    return true;
                })
                .filter(d -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String q = search.toLowerCase();
                        return d.getName().toLowerCase().contains(q) || d.getFileName().toLowerCase().contains(q);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<String> guidelines = Arrays.asList(
                "Supported formats: PDF, JPG, PNG (Max 5 MB).",
                "Ensure documents are clear and legible.",
                "File size should not exceed 5 MB per document.",
                "Documents may be subject to verification.",
                "Keep your documents updated, especially those with expiry dates."
        );

        return DocumentsPageDto.builder()
                .memberId(memberProfile.getMemberId())
                .fullName(memberProfile.getFullName())
                .memberBadge(memberProfile.getMemberBadge())
                .email(memberProfile.getEmail())
                .mobileNumber(memberProfile.getMobileNumber())
                .companyName(memberProfile.getCompanyName())
                .designation(memberProfile.getDesignation())
                .industry(memberProfile.getIndustry())
                .memberSince(memberProfile.getMemberSince())
                .avatarInitials(memberProfile.getAvatarInitials())
                .documents(filtered)
                .storageUsage(storageUsage)
                .documentGuidelines(guidelines)
                .expiryAlerts(new ArrayList<>(expiryAlerts))
                .build();
    }

    public DocumentRecordDto uploadDocument(String name, String category, String fileName, String fileUrl) {
        DocumentRecordDto newDoc = DocumentRecordDto.builder()
                .id("doc-" + docCounter.incrementAndGet())
                .name(name != null ? name : "Uploaded Document")
                .fileName(fileName != null ? fileName : "document.pdf")
                .category(category != null ? category : "Other")
                .uploadedOn(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .expiryDate("-")
                .status("Under Review")
                .fileUrl(fileUrl != null ? fileUrl : "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=600&q=80")
                .build();

        documents.add(newDoc);
        return newDoc;
    }

    // =========================================================================
    // 8. Platform Settings APIs (Image 4)
    // =========================================================================

    public PlatformSettingsPageDto getPlatformSettingsPage() {
        return PlatformSettingsPageDto.builder()
                .memberId(memberProfile.getMemberId())
                .fullName(memberProfile.getFullName())
                .memberBadge(memberProfile.getMemberBadge())
                .email(memberProfile.getEmail())
                .mobileNumber(memberProfile.getMobileNumber())
                .companyName(memberProfile.getCompanyName())
                .designation(memberProfile.getDesignation())
                .industry(memberProfile.getIndustry())
                .memberSince(memberProfile.getMemberSince())
                .avatarInitials(memberProfile.getAvatarInitials())
                .general(generalSettings)
                .platformPreferences(platformPreferences)
                .appearance(appearanceSettings)
                .languageRegion(languageRegion)
                .build();
    }

    public GeneralSettingsDto updateGeneralSettings(GeneralSettingsDto update) {
        if (update.getEmailAddress() != null) {
            generalSettings.setEmailAddress(update.getEmailAddress());
            memberProfile.setEmail(update.getEmailAddress());
        }
        if (update.getMobileNumber() != null) {
            generalSettings.setMobileNumber(update.getMobileNumber());
            memberProfile.setMobileNumber(update.getMobileNumber());
        }
        if (update.getCompany() != null) {
            generalSettings.setCompany(update.getCompany());
            memberProfile.setCompanyName(update.getCompany());
        }
        if (update.getDesignation() != null) {
            generalSettings.setDesignation(update.getDesignation());
            memberProfile.setDesignation(update.getDesignation());
        }
        return generalSettings;
    }

    public PlatformPreferencesDto updatePlatformPreferences(PlatformPreferencesDto update) {
        if (update.getReceiveEmailNotifications() != null) platformPreferences.setReceiveEmailNotifications(update.getReceiveEmailNotifications());
        if (update.getReceiveSmsNotifications() != null) platformPreferences.setReceiveSmsNotifications(update.getReceiveSmsNotifications());
        if (update.getMarketingCommunications() != null) platformPreferences.setMarketingCommunications(update.getMarketingCommunications());
        if (update.getShowPortfolioSummaryOnLogin() != null) platformPreferences.setShowPortfolioSummaryOnLogin(update.getShowPortfolioSummaryOnLogin());
        if (update.getAutoLogout() != null) platformPreferences.setAutoLogout(update.getAutoLogout());
        return platformPreferences;
    }

    public AppearanceSettingsDto updateAppearance(AppearanceSettingsDto update) {
        if (update.getSelectedTheme() != null) appearanceSettings.setSelectedTheme(update.getSelectedTheme());
        return appearanceSettings;
    }

    public LanguageRegionDto updateLanguageRegion(LanguageRegionDto update) {
        if (update.getLanguage() != null) languageRegion.setLanguage(update.getLanguage());
        if (update.getTimezone() != null) languageRegion.setTimezone(update.getTimezone());
        return languageRegion;
    }
}
