package com.atirath.trading.modules.profile.service;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.DocumentType;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.common.util.AESEncryptionUtil;
import com.atirath.trading.modules.auth.model.*;
import com.atirath.trading.modules.auth.repository.*;
import com.atirath.trading.modules.auth.service.UserDeviceSessionManager;
import com.atirath.trading.modules.market.model.AppNotification;
import com.atirath.trading.modules.market.repository.AppNotificationRepository;
import com.atirath.trading.modules.notification.model.MemberNotificationPreference;
import com.atirath.trading.modules.notification.repository.MemberNotificationPreferenceRepository;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper;
import com.atirath.trading.modules.profile.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileSettingsService {

    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter ISO_DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserAccountRepository userAccountRepository;
    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final IdentityKycDetailRepository identityKycDetailRepository;
    private final BankDetailRepository bankDetailRepository;
    private final AccountDocumentRepository accountDocumentRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserBiometricCredentialRepository userBiometricCredentialRepository;
    private final AppNotificationRepository appNotificationRepository;
    private final MemberNotificationPreferenceRepository memberNotificationPreferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final AESEncryptionUtil aesEncryptionUtil;
    private final PortfolioDataHelper portfolioDataHelper;
    private final UserDeviceSessionManager userDeviceSessionManager;

    private final ConcurrentHashMap<Long, UserProfileExtras> userExtrasMap = new ConcurrentHashMap<>();
    private final AtomicLong syntheticIdGenerator = new AtomicLong(10000L);

    private static class UserProfileExtras {
        String gender;
        String nationality = "Indian";
        String occupation;
        String linkedInProfile;
        String alternateMobile;
        String alternateEmail;
        String currentAddress;

        String companyIndustry;
        String companyType;
        String designation;
        String companyPan;
        String companyAddress;
        String website;
        String logoUrl;

        String yearsOfExperience;
        String areaOfExpertise;
        List<String> interestedSectors = new ArrayList<>();
        String investmentRange;
        String investmentPurpose;

        List<String> languageKnown = new ArrayList<>(Arrays.asList("English"));
        String howDidYouHearAboutUs;
        String profileVisibility = "Visible to verified members";

        TwoFactorSettingsDto twoFactor;
        SecurityPreferencesDto securityPreferences;
        List<ActiveSessionDto> extraSessions = new ArrayList<>();
        List<LoginHistoryItemDto> loginHistory = new ArrayList<>();
        boolean currentSessionTracked = true;

        List<BankAccountItemDto> extraBankAccounts = new ArrayList<>();
        Long overridePrimaryBankId;
        boolean primaryDbBankHidden = false;

        Map<Long, KycDocOverride> kycOverrides = new ConcurrentHashMap<>();
        List<VerificationHistoryLogDto> extraVerificationLogs = new ArrayList<>();

        PlatformPreferencesDto platformPreferences;
        AppearanceSettingsDto appearance = AppearanceSettingsDto.builder().selectedTheme("Light").build();
        LanguageRegionDto languageRegion = LanguageRegionDto.builder().language("English (India)").timezone("(UTC+05:30) Chennai, Kolkata, Mumbai, New Delhi").build();

        NotificationPreferencesDto notificationPreferences;
        NotificationChannelsDto notificationChannels;
    }

    private static class KycDocOverride {
        String status;
        String uploadedOn;
        String fileUrl;
    }

    private UserAccount loadUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found: " + userId));
    }

    private UserProfileExtras getExtras(Long userId) {
        return userExtrasMap.computeIfAbsent(userId, id -> new UserProfileExtras());
    }

    private String resolveMemberCode(UserAccount user) {
        if (user.getReferralCode() != null && !user.getReferralCode().isBlank()) {
            return user.getReferralCode().trim();
        }
        return "ATG" + String.format("%05d", user.getUserId());
    }

    private String resolveMemberBadge(UserAccount user) {
        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            return "Verified Member";
        }
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            return "Pending Verification";
        }
        return user.getAccountStatus() != null ? user.getAccountStatus().name() : "Member";
    }

    private String computeInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "MB";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.ENGLISH);
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ENGLISH);
    }

    private String formatDate(OffsetDateTime dt) {
        return dt != null ? dt.atZoneSameInstant(ZoneOffset.UTC).format(DISPLAY_DATE_FMT) : "-";
    }

    private String formatDateTime(OffsetDateTime dt) {
        return dt != null ? dt.atZoneSameInstant(ZoneOffset.UTC).format(DISPLAY_DT_FMT) : "-";
    }

    private String decryptAadhaarMasked(IdentityKycDetail kyc) {
        if (kyc == null || kyc.getAadhaarNumberEncrypted() == null || kyc.getAadhaarNumberEncrypted().isBlank()) {
            return null;
        }
        try {
            String plain = aesEncryptionUtil.decrypt(kyc.getAadhaarNumberEncrypted());
            return AESEncryptionUtil.maskAadhaar(plain);
        } catch (Exception e) {
            return AESEncryptionUtil.maskAadhaar(kyc.getAadhaarNumberEncrypted());
        }
    }

    private String decryptAccountMasked(BankDetail bank) {
        if (bank == null || bank.getAccountNumberEncrypted() == null || bank.getAccountNumberEncrypted().isBlank()) {
            return "-";
        }
        try {
            String plain = aesEncryptionUtil.decrypt(bank.getAccountNumberEncrypted());
            return AESEncryptionUtil.maskAccountNumber(plain);
        } catch (Exception e) {
            return AESEncryptionUtil.maskAccountNumber(bank.getAccountNumberEncrypted());
        }
    }

    private String resolveCompanyName(UserAccount user, CorporateProfile corp) {
        if (corp != null && corp.getCompanyName() != null && !corp.getCompanyName().isBlank()) {
            return corp.getCompanyName();
        }
        return user.getAccountType() == AccountType.CORPORATE ? "" : "Individual Investor";
    }

    private int computeProfileCompletion(UserAccount user,
                                         String fullName,
                                         IdentityKycDetail kyc,
                                         CorporateProfile corp,
                                         BankDetail bank,
                                         UserProfileExtras extras) {
        int total = 10;
        int filled = 0;
        if (isPresent(fullName)) filled++;
        if (isPresent(user.getEmail())) filled++;
        if (isPresent(user.getMobileNumber())) filled++;
        if (kyc != null && kyc.getDateOfBirth() != null) filled++;
        if (kyc != null && isPresent(kyc.getPanNumber())) filled++;
        if (kyc != null && isPresent(kyc.getAadhaarNumberEncrypted())) filled++;
        if (bank != null && isPresent(bank.getIfscCode())) filled++;
        if ((corp != null && isPresent(corp.getCompanyName())) || isPresent(extras.occupation)) filled++;
        if (isPresent(extras.currentAddress)) filled++;
        if (isPresent(extras.yearsOfExperience) || isPresent(extras.investmentRange)) filled++;
        return (int) Math.round((filled * 100.0) / total);
    }

    private boolean isPresent(String val) {
        return val != null && !val.trim().isEmpty() && !"-".equals(val.trim());
    }

    // =========================================================================
    // 1. Profile Overview & Sections
    // =========================================================================

    @Transactional(readOnly = true)
    public MemberProfileDto getProfile(Long userId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        IndividualProfile indiv = individualProfileRepository.findByUserAccount_UserId(userId).orElse(null);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);
        IdentityKycDetail kyc = identityKycDetailRepository.findByUserAccount_UserId(userId).orElse(null);
        BankDetail bank = bankDetailRepository.findByUserAccount_UserId(userId).orElse(null);

        String maskedAadhaar = decryptAadhaarMasked(kyc);
        String aadhaarStatus = kyc != null && isPresent(kyc.getAadhaarNumberEncrypted())
                ? (user.getAccountStatus() == AccountStatus.ACTIVE ? "Verified" : "Submitted")
                : "Not Provided";

        String gender = (indiv != null && isPresent(indiv.getGender())) ? indiv.getGender() : (extras.gender != null ? extras.gender : "");
        String companyName = (corp != null && isPresent(corp.getCompanyName()))
                ? corp.getCompanyName()
                : ((indiv != null && isPresent(indiv.getCompanyName())) ? indiv.getCompanyName() : "");
        String industry = (corp != null && isPresent(corp.getIndustry()))
                ? corp.getIndustry()
                : (extras.companyIndustry != null ? extras.companyIndustry : "");
        String companyType = (corp != null && isPresent(corp.getCompanyType()))
                ? corp.getCompanyType()
                : (extras.companyType != null ? extras.companyType : (corp != null ? "Registered Entity" : ""));
        String companyPan = (kyc != null && isPresent(kyc.getCompanyPanNumber()))
                ? kyc.getCompanyPanNumber()
                : (extras.companyPan != null ? extras.companyPan : "");

        PersonalInfoDto personal = PersonalInfoDto.builder()
                .fullName(fullName)
                .dateOfBirth(kyc != null && kyc.getDateOfBirth() != null ? kyc.getDateOfBirth().format(DISPLAY_DATE_FMT) : "")
                .gender(gender)
                .nationality(extras.nationality != null ? extras.nationality : "Indian")
                .panNumber(kyc != null && kyc.getPanNumber() != null ? kyc.getPanNumber() : "")
                .aadhaarNumber(maskedAadhaar != null ? maskedAadhaar : "")
                .aadhaarStatus(aadhaarStatus)
                .occupation(extras.occupation != null ? extras.occupation : (user.getAccountType() == AccountType.CORPORATE ? "Corporate Member" : "Individual Investor"))
                .linkedInProfile(extras.linkedInProfile != null ? extras.linkedInProfile : "")
                .build();

        OrganizationInfoDto org = OrganizationInfoDto.builder()
                .companyName(companyName)
                .industry(industry)
                .companyType(companyType)
                .designation(extras.designation != null ? extras.designation : (corp != null && isPresent(corp.getContactPersonName()) ? "Authorized Signatory" : ""))
                .companyPan(companyPan)
                .registrationNumber(corp != null && corp.getRegistrationId() != null ? corp.getRegistrationId() : "")
                .companyAddress(extras.companyAddress != null ? extras.companyAddress : "")
                .website(extras.website != null ? extras.website : "")
                .logoUrl(extras.logoUrl)
                .build();

        ContactInfoDto contact = ContactInfoDto.builder()
                .emailAddress(user.getEmail())
                .emailStatus(user.getAccountStatus() == AccountStatus.ACTIVE ? "Verified" : "Pending")
                .mobileNumber(user.getMobileNumber())
                .mobileStatus(isPresent(user.getMobileNumber()) ? "Verified" : "Pending")
                .alternateMobile(extras.alternateMobile != null ? extras.alternateMobile : "")
                .alternateEmail(extras.alternateEmail != null ? extras.alternateEmail : "")
                .build();

        ProfessionalDetailsDto prof = ProfessionalDetailsDto.builder()
                .yearsOfExperience(extras.yearsOfExperience != null ? extras.yearsOfExperience : "")
                .areaOfExpertise(extras.areaOfExpertise != null ? extras.areaOfExpertise : "")
                .interestedSectors(new ArrayList<>(extras.interestedSectors))
                .investmentRange(extras.investmentRange != null ? extras.investmentRange : "")
                .investmentPurpose(extras.investmentPurpose != null ? extras.investmentPurpose : "")
                .build();

        AddressInfoDto addr = AddressInfoDto.builder()
                .currentAddress(extras.currentAddress != null ? extras.currentAddress : "")
                .build();

        AdditionalInfoDto addl = AdditionalInfoDto.builder()
                .languageKnown(new ArrayList<>(extras.languageKnown))
                .howDidYouHearAboutUs(extras.howDidYouHearAboutUs != null ? extras.howDidYouHearAboutUs : "")
                .profileVisibility(extras.profileVisibility)
                .build();

        int completion = computeProfileCompletion(user, fullName, kyc, corp, bank, extras);

        return MemberProfileDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(org.getDesignation()) ? org.getDesignation() : personal.getOccupation())
                .industry(isPresent(org.getIndustry()) ? org.getIndustry() : "Private Equity & Unlisted Shares")
                .memberSince(formatDate(user.getCreatedAt()))
                .profileCompletion(completion)
                .avatarInitials(computeInitials(fullName))
                .personalInfo(personal)
                .organizationInfo(org)
                .contactInfo(contact)
                .professionalDetails(prof)
                .addressInfo(addr)
                .additionalInfo(addl)
                .build();
    }

    @Transactional
    public PersonalInfoDto updatePersonalInfo(Long userId, PersonalInfoDto update) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        if (isPresent(update.getFullName())) {
            String trimmedName = update.getFullName().trim();
            if (user.getAccountType() == AccountType.CORPORATE) {
                corporateProfileRepository.findByUserAccount_UserId(userId).ifPresent(corp -> {
                    corp.setContactPersonName(trimmedName);
                    corporateProfileRepository.save(corp);
                });
            } else {
                IndividualProfile indiv = individualProfileRepository.findByUserAccount_UserId(userId)
                        .orElseGet(() -> IndividualProfile.builder().userAccount(user).fullName(trimmedName).build());
                indiv.setFullName(trimmedName);
                individualProfileRepository.save(indiv);
            }
        }

        if (isPresent(update.getPanNumber()) || isPresent(update.getDateOfBirth())) {
            identityKycDetailRepository.findByUserAccount_UserId(userId).ifPresent(kyc -> {
                if (isPresent(update.getPanNumber())) {
                    String normalizedPan = update.getPanNumber().trim().toUpperCase(Locale.ENGLISH);
                    if (!normalizedPan.equalsIgnoreCase(kyc.getPanNumber())) {
                        if (identityKycDetailRepository.existsByPanNumber(normalizedPan)) {
                            throw new ApiException("PAN number is already registered to another account", HttpStatus.BAD_REQUEST);
                        }
                        kyc.setPanNumber(normalizedPan);
                    }
                }
                if (isPresent(update.getDateOfBirth())) {
                    LocalDate parsedDob = tryParseDate(update.getDateOfBirth());
                    if (parsedDob != null) {
                        kyc.setDateOfBirth(parsedDob);
                    }
                }
                identityKycDetailRepository.save(kyc);
            });
        }

        if (update.getGender() != null) {
            String g = update.getGender().trim();
            extras.gender = g;
            if (user.getAccountType() == AccountType.INDIVIDUAL) {
                individualProfileRepository.findByUserAccount_UserId(userId).ifPresent(indiv -> {
                    indiv.setGender(g);
                    individualProfileRepository.save(indiv);
                });
            }
        }
        if (update.getNationality() != null) extras.nationality = update.getNationality().trim();
        if (update.getOccupation() != null) extras.occupation = update.getOccupation().trim();
        if (update.getLinkedInProfile() != null) extras.linkedInProfile = update.getLinkedInProfile().trim();

        return getProfile(userId).getPersonalInfo();
    }

    private LocalDate tryParseDate(String value) {
        String trimmed = value.trim();
        for (DateTimeFormatter fmt : List.of(ISO_DATE_FMT, DISPLAY_DATE_FMT, DateTimeFormatter.ofPattern("dd-MM-yyyy"), DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    @Transactional
    public OrganizationInfoDto updateOrganizationInfo(Long userId, OrganizationInfoDto update) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        if (isPresent(update.getCompanyName()) || isPresent(update.getRegistrationNumber()) || update.getIndustry() != null || update.getCompanyType() != null) {
            CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);
            if (corp != null) {
                if (isPresent(update.getCompanyName())) corp.setCompanyName(update.getCompanyName().trim());
                if (isPresent(update.getRegistrationNumber())) corp.setRegistrationId(update.getRegistrationNumber().trim());
                if (update.getIndustry() != null) corp.setIndustry(update.getIndustry().trim());
                if (update.getCompanyType() != null) corp.setCompanyType(update.getCompanyType().trim());
                corporateProfileRepository.save(corp);
            } else if (user.getAccountType() == AccountType.INDIVIDUAL && isPresent(update.getCompanyName())) {
                individualProfileRepository.findByUserAccount_UserId(userId).ifPresent(indiv -> {
                    indiv.setCompanyName(update.getCompanyName().trim());
                    individualProfileRepository.save(indiv);
                });
            } else if (isPresent(update.getCompanyName()) && isPresent(update.getRegistrationNumber())) {
                String regId = update.getRegistrationNumber().trim();
                if (corporateProfileRepository.existsByRegistrationId(regId)) {
                    throw new ApiException("Registration number already exists", HttpStatus.BAD_REQUEST);
                }
                CorporateProfile newCorp = CorporateProfile.builder()
                        .userAccount(user)
                        .companyName(update.getCompanyName().trim())
                        .registrationId(regId)
                        .contactPersonName(portfolioDataHelper.resolveUserFullName(user))
                        .industry(update.getIndustry() != null ? update.getIndustry().trim() : null)
                        .companyType(update.getCompanyType() != null ? update.getCompanyType().trim() : null)
                        .build();
                corporateProfileRepository.save(newCorp);
            }
        }

        if (update.getIndustry() != null) extras.companyIndustry = update.getIndustry().trim();
        if (update.getCompanyType() != null) extras.companyType = update.getCompanyType().trim();
        if (update.getDesignation() != null) extras.designation = update.getDesignation().trim();
        if (update.getCompanyPan() != null) {
            String cp = update.getCompanyPan().trim().toUpperCase(Locale.ENGLISH);
            extras.companyPan = cp;
            identityKycDetailRepository.findByUserAccount_UserId(userId).ifPresent(kyc -> {
                kyc.setCompanyPanNumber(cp);
                identityKycDetailRepository.save(kyc);
            });
        }
        if (update.getCompanyAddress() != null) extras.companyAddress = update.getCompanyAddress().trim();
        if (update.getWebsite() != null) extras.website = update.getWebsite().trim();
        if (update.getLogoUrl() != null) extras.logoUrl = update.getLogoUrl().trim();

        return getProfile(userId).getOrganizationInfo();
    }

    @Transactional
    public ContactInfoDto updateContactInfo(Long userId, ContactInfoDto update) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        if (isPresent(update.getMobileNumber())) {
            String newMobile = update.getMobileNumber().trim();
            if (!newMobile.equals(user.getMobileNumber())) {
                if (userAccountRepository.existsByMobileNumber(newMobile)) {
                    throw new ApiException("Mobile number is already associated with another account", HttpStatus.BAD_REQUEST);
                }
                user.setMobileNumber(newMobile);
                userAccountRepository.save(user);
            }
        }
        if (update.getAlternateMobile() != null) extras.alternateMobile = update.getAlternateMobile().trim();
        if (update.getAlternateEmail() != null) extras.alternateEmail = update.getAlternateEmail().trim();

        return getProfile(userId).getContactInfo();
    }

    public ProfessionalDetailsDto updateProfessionalDetails(Long userId, ProfessionalDetailsDto update) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (update.getYearsOfExperience() != null) extras.yearsOfExperience = update.getYearsOfExperience().trim();
        if (update.getAreaOfExpertise() != null) extras.areaOfExpertise = update.getAreaOfExpertise().trim();
        if (update.getInterestedSectors() != null) extras.interestedSectors = new ArrayList<>(update.getInterestedSectors());
        if (update.getInvestmentRange() != null) extras.investmentRange = update.getInvestmentRange().trim();
        if (update.getInvestmentPurpose() != null) extras.investmentPurpose = update.getInvestmentPurpose().trim();
        return getProfile(userId).getProfessionalDetails();
    }

    public AddressInfoDto updateAddressInfo(Long userId, AddressInfoDto update) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (update.getCurrentAddress() != null) extras.currentAddress = update.getCurrentAddress().trim();
        return getProfile(userId).getAddressInfo();
    }

    public AdditionalInfoDto updateAdditionalInfo(Long userId, AdditionalInfoDto update) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (update.getLanguageKnown() != null) extras.languageKnown = new ArrayList<>(update.getLanguageKnown());
        if (update.getHowDidYouHearAboutUs() != null) extras.howDidYouHearAboutUs = update.getHowDidYouHearAboutUs().trim();
        if (update.getProfileVisibility() != null) extras.profileVisibility = update.getProfileVisibility().trim();
        return getProfile(userId).getAdditionalInfo();
    }

    // =========================================================================
    // 2. KYC & Verification
    // =========================================================================

    @Transactional(readOnly = true)
    public KycVerificationPageDto getKycVerification(Long userId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);
        IdentityKycDetail kyc = identityKycDetailRepository.findByUserAccount_UserId(userId).orElse(null);
        BankDetail bank = bankDetailRepository.findByUserAccount_UserId(userId).orElse(null);
        List<AccountDocument> dbDocs = accountDocumentRepository.findByUserAccount_UserId(userId);

        Map<DocumentType, AccountDocument> latestDocByType = new EnumMap<>(DocumentType.class);
        for (AccountDocument doc : dbDocs) {
            if (doc.getDocumentType() != null) {
                latestDocByType.put(doc.getDocumentType(), doc);
            }
        }

        boolean isAccountActive = user.getAccountStatus() == AccountStatus.ACTIVE;

        AccountDocument idDoc = latestDocByType.get(DocumentType.IDENTITY_PROOF);
        AccountDocument addrDoc = latestDocByType.get(DocumentType.ADDRESS_PROOF);
        AccountDocument bankDoc = latestDocByType.get(DocumentType.BANK_PASSBOOK);
        AccountDocument livePhotoDoc = latestDocByType.get(DocumentType.LIVE_PHOTO);

        List<KycDocumentItemDto> docs = new ArrayList<>();

        docs.add(buildKycItem(
                1L,
                "Identity Proof (PAN)",
                "Upload your PAN card for identity verification.",
                idDoc != null ? idDoc.getDocumentName() : "PAN Card",
                kyc != null && isPresent(kyc.getPanNumber()) ? kyc.getPanNumber() : "-",
                idDoc,
                kyc != null && isPresent(kyc.getPanNumber()),
                isAccountActive,
                extras.kycOverrides.get(1L)
        ));

        String maskedAadhaar = decryptAadhaarMasked(kyc);
        docs.add(buildKycItem(
                2L,
                "Address Proof",
                "Upload address proof (Aadhaar, Passport or Voter ID).",
                addrDoc != null ? addrDoc.getDocumentName() : "Aadhaar / Address Proof",
                maskedAadhaar != null ? maskedAadhaar : "-",
                addrDoc,
                kyc != null && isPresent(kyc.getAadhaarNumberEncrypted()),
                isAccountActive,
                extras.kycOverrides.get(2L)
        ));

        docs.add(buildKycItem(
                3L,
                "Organization Documents",
                "Company registration certificate (required for Corporate members).",
                corp != null ? "Registration Certificate" : "Corporate Incorporation",
                corp != null && isPresent(corp.getRegistrationId()) ? corp.getRegistrationId() : "-",
                null,
                corp != null && isPresent(corp.getRegistrationId()),
                corp != null && isAccountActive,
                extras.kycOverrides.get(3L)
        ));

        docs.add(buildKycItem(
                4L,
                "Bank Details Proof",
                "Upload cancelled cheque or bank passbook statement.",
                bankDoc != null ? bankDoc.getDocumentName() : "Cancelled Cheque / Passbook",
                bank != null && isPresent(bank.getIfscCode()) ? bank.getIfscCode() : "-",
                bankDoc,
                bank != null && isPresent(bank.getIfscCode()),
                isAccountActive && bankDoc != null,
                extras.kycOverrides.get(4L)
        ));

        docs.add(buildKycItem(
                5L,
                "Live Photo / Self Declaration",
                "Upload recent live photo or signed compliance declaration.",
                livePhotoDoc != null ? livePhotoDoc.getDocumentName() : "Live Photo / Self Declaration",
                livePhotoDoc != null ? "DOC-" + livePhotoDoc.getDocumentId() : "-",
                livePhotoDoc,
                livePhotoDoc != null,
                isAccountActive && livePhotoDoc != null,
                extras.kycOverrides.get(5L)
        ));

        int verified = (int) docs.stream().filter(d -> "Verified".equalsIgnoreCase(d.getStatus())).count();
        int underReview = (int) docs.stream().filter(d -> "Under Review".equalsIgnoreCase(d.getStatus())).count();
        int notVerified = (int) docs.stream().filter(d -> "Not Verified".equalsIgnoreCase(d.getStatus())).count();
        int notRequired = 0;

        if (user.getAccountType() != AccountType.CORPORATE && corp == null && extras.kycOverrides.get(3L) == null) {
            docs.get(2).setStatus("Not Required");
            docs.get(2).setCanUpload(false);
            verified = (int) docs.stream().filter(d -> "Verified".equalsIgnoreCase(d.getStatus())).count();
            underReview = (int) docs.stream().filter(d -> "Under Review".equalsIgnoreCase(d.getStatus())).count();
            notVerified = (int) docs.stream().filter(d -> "Not Verified".equalsIgnoreCase(d.getStatus())).count();
            notRequired = 1;
        }

        int applicableTotal = Math.max(1, docs.size() - notRequired);
        int completionPct = (int) Math.round(((verified + underReview * 0.5) * 100.0) / applicableTotal);
        completionPct = Math.min(100, Math.max(0, completionPct));

        int activeStep = Math.min(5, Math.max(1, verified + 1));
        String alertTitle = verified >= applicableTotal
                ? "All required KYC verifications are complete."
                : "KYC verification in progress (" + verified + " of " + applicableTotal + " verified).";
        String alertMsg = verified >= applicableTotal
                ? "Your account has full platform trading and settlement access."
                : "Upload or review any pending documents below to unlock full trading features.";

        List<VerificationHistoryLogDto> history = new ArrayList<>(extras.extraVerificationLogs);
        if (kyc != null) {
            history.add(VerificationHistoryLogDto.builder()
                    .title("Identity KYC Recorded (" + kyc.getPanNumber() + ")")
                    .timestamp(formatDateTime(kyc.getCreatedAt()))
                    .actor(fullName)
                    .status(isAccountActive ? "Verified" : "Submitted")
                    .build());
        }
        if (bank != null) {
            history.add(VerificationHistoryLogDto.builder()
                    .title("Bank Details Recorded (" + bank.getBankName() + ")")
                    .timestamp(formatDateTime(bank.getCreatedAt()))
                    .actor(fullName)
                    .status(isAccountActive ? "Verified" : "Submitted")
                    .build());
        }
        for (AccountDocument doc : dbDocs) {
            history.add(VerificationHistoryLogDto.builder()
                    .title(doc.getDocumentType().name().replace('_', ' ') + " Uploaded")
                    .timestamp(formatDateTime(doc.getUploadedAt()))
                    .actor(fullName)
                    .status(isAccountActive ? "Verified" : "Submitted")
                    .build());
        }
        if (history.isEmpty()) {
            history.add(VerificationHistoryLogDto.builder()
                    .title("KYC Onboarding Pending")
                    .timestamp(formatDateTime(user.getCreatedAt()))
                    .actor("System")
                    .status("Pending")
                    .build());
        }

        return KycVerificationPageDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : (user.getAccountType() == AccountType.CORPORATE ? "Authorized Signatory" : "Individual Investor"))
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .activeStep(activeStep)
                .stepperSteps(Arrays.asList("Identity Verification", "Address Verification", "Organization Verification", "Bank Verification", "Final Review"))
                .completionPercentage(completionPct)
                .verifiedCount(verified)
                .underReviewCount(underReview)
                .notVerifiedCount(notVerified)
                .notRequiredCount(notRequired)
                .statusAlertTitle(alertTitle)
                .statusAlertMessage(alertMsg)
                .importantNotes(Arrays.asList(
                        "All documents must be clear and valid.",
                        "Accepted formats: PDF, JPG, PNG (Max 5 MB).",
                        "Ensure document details match your registered KYC profile.",
                        "Verification review typically completes within 1-2 business days."
                ))
                .documents(docs)
                .verificationHistory(history)
                .build();
    }

    private KycDocumentItemDto buildKycItem(Long id,
                                            String title,
                                            String description,
                                            String docTypeLabel,
                                            String docNumber,
                                            AccountDocument doc,
                                            boolean dataSubmitted,
                                            boolean accountVerified,
                                            KycDocOverride override) {
        String uploadedOn = doc != null && doc.getUploadedAt() != null ? formatDate(doc.getUploadedAt()) : "-";
        String fileUrl = doc != null ? doc.getFilePath() : null;
        String status;
        boolean canUpload;

        if (override != null) {
            status = override.status;
            uploadedOn = override.uploadedOn;
            fileUrl = override.fileUrl;
            canUpload = !"Verified".equalsIgnoreCase(status);
        } else if (accountVerified && (doc != null || dataSubmitted)) {
            status = "Verified";
            canUpload = false;
        } else if (doc != null || dataSubmitted) {
            status = "Under Review";
            canUpload = true;
        } else {
            status = "Not Verified";
            canUpload = true;
        }

        return KycDocumentItemDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .documentType(docTypeLabel)
                .documentNumber(docNumber)
                .uploadedOn(uploadedOn)
                .status(status)
                .fileUrl(fileUrl)
                .canUpload(canUpload)
                .build();
    }

    @Transactional
    public KycDocumentItemDto uploadKycDocument(Long userId, Long docId, String fileUrl) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        KycVerificationPageDto page = getKycVerification(userId);

        KycDocumentItemDto target = page.getDocuments().stream()
                .filter(d -> d.getId().equals(docId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("KYC Document not found: " + docId));

        String resolvedPath = isPresent(fileUrl) ? fileUrl.trim() : "kyc-upload-" + userId + "-" + docId + ".pdf";
        String nowDate = LocalDate.now().format(DISPLAY_DATE_FMT);

        DocumentType mappedType = switch (docId.intValue()) {
            case 1 -> DocumentType.IDENTITY_PROOF;
            case 2, 3 -> DocumentType.ADDRESS_PROOF;
            case 4 -> DocumentType.BANK_PASSBOOK;
            default -> DocumentType.LIVE_PHOTO;
        };

        AccountDocument savedDoc = AccountDocument.builder()
                .userAccount(user)
                .documentType(mappedType)
                .documentName(target.getTitle())
                .filePath(resolvedPath)
                .mimeType(resolvedPath.toLowerCase(Locale.ENGLISH).endsWith(".png") ? "image/png" : "application/pdf")
                .fileSizeBytes(245760L)
                .build();
        accountDocumentRepository.save(savedDoc);

        KycDocOverride override = new KycDocOverride();
        override.status = "Under Review";
        override.uploadedOn = nowDate;
        override.fileUrl = resolvedPath;
        extras.kycOverrides.put(docId, override);

        extras.extraVerificationLogs.add(0, VerificationHistoryLogDto.builder()
                .title(target.getTitle() + " Submitted")
                .timestamp(OffsetDateTime.now().format(DISPLAY_DT_FMT))
                .actor(fullName)
                .status("Submitted")
                .build());

        target.setStatus("Under Review");
        target.setUploadedOn(nowDate);
        target.setFileUrl(resolvedPath);
        target.setCanUpload(false);
        return target;
    }

    // =========================================================================
    // 3. Bank & Settlement Accounts
    // =========================================================================

    @Transactional(readOnly = true)
    public BankAccountsPageDto getBankAccountsPage(Long userId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);

        List<BankAccountItemDto> list = buildBankAccountsList(user, extras);

        return BankAccountsPageDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : "Member")
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .bankAccounts(list)
                .accountGuidelines(Arrays.asList(
                        "Bank account must be in your name or registered organization name.",
                        "Only Indian bank accounts are supported.",
                        "Account must be verified to receive settlements.",
                        "Ensure correct IFSC and account details.",
                        "You can add multiple bank accounts and set one as primary."
                ))
                .settlementInfo("Your primary bank account will be used for all settlements, payouts and refunds across the platform.")
                .build();
    }

    private List<BankAccountItemDto> buildBankAccountsList(UserAccount user, UserProfileExtras extras) {
        List<BankAccountItemDto> result = new ArrayList<>();
        Optional<BankDetail> dbBankOpt = bankDetailRepository.findByUserAccount_UserId(user.getUserId());
        if (dbBankOpt.isPresent() && !extras.primaryDbBankHidden) {
            BankDetail dbBank = dbBankOpt.get();
            boolean isPrimary = extras.overridePrimaryBankId == null
                    || Objects.equals(extras.overridePrimaryBankId, dbBank.getBankId());
            String bankLogo = isPresent(dbBank.getBankName())
                    ? dbBank.getBankName().trim().split("\\s+")[0].toUpperCase(Locale.ENGLISH)
                    : "BANK";
            result.add(BankAccountItemDto.builder()
                    .id(dbBank.getBankId())
                    .bankName(dbBank.getBankName())
                    .bankLogo(bankLogo)
                    .accountHolderName(dbBank.getAccountHolderName())
                    .accountNumber(decryptAccountMasked(dbBank))
                    .ifscCode(dbBank.getIfscCode())
                    .isPrimary(isPrimary)
                    .status(user.getAccountStatus() == AccountStatus.ACTIVE ? "Verified" : "Under Verification")
                    .addedOn(formatDate(dbBank.getCreatedAt()))
                    .build());
        }
        for (BankAccountItemDto extra : extras.extraBankAccounts) {
            boolean isPrimary = extras.overridePrimaryBankId != null
                    ? Objects.equals(extras.overridePrimaryBankId, extra.getId())
                    : Boolean.TRUE.equals(extra.getIsPrimary());
            extra.setIsPrimary(isPrimary);
            result.add(extra);
        }
        return result;
    }

    @Transactional
    public BankAccountItemDto addBankAccount(Long userId, AddBankAccountRequestDto input) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        Optional<BankDetail> existingDb = bankDetailRepository.findByUserAccount_UserId(userId);
        if (existingDb.isEmpty() || extras.primaryDbBankHidden) {
            String encryptedAcc = aesEncryptionUtil.encrypt(input.getAccountNumber().trim());
            BankDetail entity = existingDb.orElseGet(() -> BankDetail.builder().userAccount(user).build());
            entity.setBankName(input.getBankName().trim());
            entity.setAccountHolderName(input.getAccountHolderName().trim());
            entity.setAccountNumberEncrypted(encryptedAcc);
            entity.setIfscCode(input.getIfscCode().trim().toUpperCase(Locale.ENGLISH));
            entity.setBranchName("Primary Branch");
            BankDetail saved = bankDetailRepository.save(entity);
            extras.primaryDbBankHidden = false;
            if (Boolean.TRUE.equals(input.getSetAsPrimary()) || extras.extraBankAccounts.isEmpty()) {
                extras.overridePrimaryBankId = saved.getBankId();
            }
            return buildBankAccountsList(user, extras).stream()
                    .filter(b -> Objects.equals(b.getId(), saved.getBankId()))
                    .findFirst()
                    .orElseThrow();
        }

        long newId = syntheticIdGenerator.incrementAndGet();
        boolean makePrimary = Boolean.TRUE.equals(input.getSetAsPrimary());
        if (makePrimary) {
            extras.overridePrimaryBankId = newId;
            extras.extraBankAccounts.forEach(b -> b.setIsPrimary(false));
        }

        String rawAcc = input.getAccountNumber().trim();
        String maskedAcc = rawAcc.length() > 4
                ? "**** **** " + rawAcc.substring(rawAcc.length() - 4)
                : rawAcc;
        String logo = input.getBankName().trim().split("\\s+")[0].toUpperCase(Locale.ENGLISH);

        BankAccountItemDto added = BankAccountItemDto.builder()
                .id(newId)
                .bankName(input.getBankName().trim())
                .bankLogo(logo)
                .accountHolderName(input.getAccountHolderName().trim())
                .accountNumber(maskedAcc)
                .ifscCode(input.getIfscCode().trim().toUpperCase(Locale.ENGLISH))
                .isPrimary(makePrimary)
                .status("Under Verification")
                .addedOn(LocalDate.now().format(DISPLAY_DATE_FMT))
                .build();
        extras.extraBankAccounts.add(added);
        return added;
    }

    @Transactional
    public BankAccountItemDto setPrimaryAccount(Long userId, Long accountId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        List<BankAccountItemDto> list = buildBankAccountsList(user, extras);

        BankAccountItemDto target = list.stream()
                .filter(b -> Objects.equals(b.getId(), accountId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found: " + accountId));

        extras.overridePrimaryBankId = accountId;
        extras.extraBankAccounts.forEach(b -> b.setIsPrimary(Objects.equals(b.getId(), accountId)));
        target.setIsPrimary(true);
        return target;
    }

    @Transactional
    public void removeBankAccount(Long userId, Long accountId) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        boolean removedExtra = extras.extraBankAccounts.removeIf(b -> Objects.equals(b.getId(), accountId));
        if (removedExtra) {
            if (Objects.equals(extras.overridePrimaryBankId, accountId)) {
                extras.overridePrimaryBankId = null;
            }
            return;
        }

        Optional<BankDetail> dbBankOpt = bankDetailRepository.findByUserAccount_UserId(userId);
        if (dbBankOpt.isPresent() && Objects.equals(dbBankOpt.get().getBankId(), accountId)) {
            extras.primaryDbBankHidden = true;
            if (!extras.extraBankAccounts.isEmpty()) {
                extras.overridePrimaryBankId = extras.extraBankAccounts.get(0).getId();
                extras.extraBankAccounts.get(0).setIsPrimary(true);
            } else {
                extras.overridePrimaryBankId = null;
            }
            return;
        }
        throw new ResourceNotFoundException("Bank account not found: " + accountId);
    }

    // =========================================================================
    // 4. Security & Login Activity
    // =========================================================================

    @Transactional(readOnly = true)
    public SecurityOverviewDto getSecurityOverview(Long userId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);

        UserCredential cred = userCredentialRepository.findByUserAccount_UserId(userId).orElse(null);
        String passwordChanged = cred != null && cred.getPasswordChangedAt() != null
                ? formatDate(cred.getPasswordChangedAt())
                : formatDate(user.getCreatedAt());

        if (extras.twoFactor == null) {
            extras.twoFactor = TwoFactorSettingsDto.builder()
                    .isEnabled(false)
                    .authenticatorAppStatus("Not Configured")
                    .authenticatorAppEnabled(false)
                    .smsPhoneNumber(user.getMobileNumber())
                    .smsVerificationEnabled(false)
                    .backupCodesCount(0)
                    .build();
        } else {
            extras.twoFactor.setSmsPhoneNumber(user.getMobileNumber());
        }

        if (extras.securityPreferences == null) {
            extras.securityPreferences = SecurityPreferencesDto.builder()
                    .emailNotificationsForNewLogin(true)
                    .loginAlertsForUnknownDevices(true)
                    .require2faForAllLogins(Boolean.TRUE.equals(extras.twoFactor.getIsEnabled()))
                    .autoLogoutForInactiveSessions(false)
                    .build();
        }

        List<ActiveSessionDto> sessions = new ArrayList<>();
        if (extras.currentSessionTracked) {
            sessions.add(ActiveSessionDto.builder()
                    .sessionId("sess-current")
                    .device("Current Web Browser Session")
                    .location("Active Session")
                    .ipAddress("Current Device")
                    .isCurrentSession(true)
                    .build());
        }
        List<UserBiometricCredential> biometrics = userBiometricCredentialRepository.findByUserAccount_UserId(userId);
        for (UserBiometricCredential bio : biometrics) {
            if (Boolean.TRUE.equals(bio.getIsActive())) {
                sessions.add(ActiveSessionDto.builder()
                        .sessionId("bio-" + bio.getCredentialId())
                        .device(isPresent(bio.getDeviceName()) ? bio.getDeviceName() : "Trusted Mobile Device")
                        .location("Enrolled " + formatDate(bio.getEnrolledAt()))
                        .ipAddress(bio.getDeviceId())
                        .isCurrentSession(false)
                        .build());
            }
        }
        sessions.addAll(extras.extraSessions);

        List<LoginHistoryItemDto> history = new ArrayList<>(extras.loginHistory);
        for (UserBiometricCredential bio : biometrics) {
            OffsetDateTime activityTs = bio.getLastUsedAt() != null ? bio.getLastUsedAt() : bio.getEnrolledAt();
            history.add(LoginHistoryItemDto.builder()
                    .dateTime(formatDateTime(activityTs))
                    .device(isPresent(bio.getDeviceName()) ? bio.getDeviceName() : bio.getDeviceId())
                    .location("Trusted Device")
                    .ipAddress(bio.getDeviceId())
                    .status(Boolean.TRUE.equals(bio.getIsActive()) ? "Successful" : "Revoked")
                    .build());
        }
        if (cred != null && cred.getPasswordChangedAt() != null) {
            history.add(LoginHistoryItemDto.builder()
                    .dateTime(formatDateTime(cred.getPasswordChangedAt()))
                    .device("Web / Mobile Auth")
                    .location("Account Security")
                    .ipAddress("-")
                    .status("Successful")
                    .build());
        }

        return SecurityOverviewDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : "Member")
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .maskedPassword("••••••••••••")
                .passwordLastChanged(passwordChanged)
                .twoFactor(extras.twoFactor)
                .activeSessions(sessions)
                .securityPreferences(extras.securityPreferences)
                .loginHistory(history)
                .build();
    }

    @Transactional
    public boolean changePassword(Long userId, ChangePasswordRequestDto input) {
        UserAccount user = loadUser(userId);
        if (!Objects.equals(input.getNewPassword(), input.getConfirmPassword())) {
            throw new ApiException("New password and confirmation password do not match", HttpStatus.BAD_REQUEST);
        }
        if (input.getNewPassword().length() < 8) {
            throw new ApiException("New password must be at least 8 characters long", HttpStatus.BAD_REQUEST);
        }

        UserCredential cred = userCredentialRepository.findByUserAccount_UserId(userId)
                .orElseThrow(() -> new ApiException("Password credentials not initialized for this account", HttpStatus.BAD_REQUEST));

        if (!passwordEncoder.matches(input.getCurrentPassword(), cred.getPasswordHash())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }

        cred.setPasswordHash(passwordEncoder.encode(input.getNewPassword()));
        cred.setPasswordChangedAt(OffsetDateTime.now());
        userCredentialRepository.save(cred);

        getExtras(userId).loginHistory.add(0, LoginHistoryItemDto.builder()
                .dateTime(OffsetDateTime.now().format(DISPLAY_DT_FMT))
                .device("Web Browser")
                .location("Password Updated")
                .ipAddress("-")
                .status("Successful")
                .build());
        return true;
    }

    public TwoFactorSettingsDto toggleTwoFactor(Long userId, boolean enable) {
        SecurityOverviewDto overview = getSecurityOverview(userId);
        TwoFactorSettingsDto twoFactor = overview.getTwoFactor();
        twoFactor.setIsEnabled(enable);
        twoFactor.setAuthenticatorAppEnabled(enable);
        twoFactor.setAuthenticatorAppStatus(enable ? "Connected" : "Not Configured");
        twoFactor.setBackupCodesCount(enable ? 8 : 0);
        return twoFactor;
    }

    public SecurityPreferencesDto updateSecurityPreferences(Long userId, SecurityPreferencesDto update) {
        SecurityOverviewDto overview = getSecurityOverview(userId);
        SecurityPreferencesDto prefs = overview.getSecurityPreferences();
        if (update.getEmailNotificationsForNewLogin() != null)
            prefs.setEmailNotificationsForNewLogin(update.getEmailNotificationsForNewLogin());
        if (update.getLoginAlertsForUnknownDevices() != null)
            prefs.setLoginAlertsForUnknownDevices(update.getLoginAlertsForUnknownDevices());
        if (update.getRequire2faForAllLogins() != null)
            prefs.setRequire2faForAllLogins(update.getRequire2faForAllLogins());
        if (update.getAutoLogoutForInactiveSessions() != null)
            prefs.setAutoLogoutForInactiveSessions(update.getAutoLogoutForInactiveSessions());
        return prefs;
    }

    @Transactional
    public void terminateSession(Long userId, String sessionId) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (sessionId != null && sessionId.startsWith("bio-")) {
            Long credId = Long.parseLong(sessionId.substring(4));
            userBiometricCredentialRepository.findById(credId)
                    .filter(b -> Objects.equals(b.getUserAccount().getUserId(), userId))
                    .ifPresent(b -> {
                        b.setIsActive(false);
                        userBiometricCredentialRepository.save(b);
                    });
            return;
        }
        extras.extraSessions.removeIf(s -> Objects.equals(s.getSessionId(), sessionId) && !Boolean.TRUE.equals(s.getIsCurrentSession()));
    }

    @Transactional
    public void terminateAllOtherSessions(Long userId) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        extras.extraSessions.removeIf(s -> !Boolean.TRUE.equals(s.getIsCurrentSession()));
        List<UserBiometricCredential> biometrics = userBiometricCredentialRepository.findByUserAccount_UserId(userId);
        for (UserBiometricCredential bio : biometrics) {
            if (Boolean.TRUE.equals(bio.getIsActive())) {
                bio.setIsActive(false);
                userBiometricCredentialRepository.save(bio);
            }
        }
    }

    // =========================================================================
    // 5. Active Devices
    // =========================================================================

    @Transactional(readOnly = true)
    public ActiveDevicesPageDto getActiveDevicesPage(Long userId) {
        return getActiveDevicesPage(userId, null);
    }

    @Transactional(readOnly = true)
    public ActiveDevicesPageDto getActiveDevicesPage(Long userId, String currentDeviceId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        SecurityOverviewDto sec = getSecurityOverview(userId);

        List<com.atirath.trading.modules.auth.dto.device.ActiveDeviceSessionDto> activeSessions =
                userDeviceSessionManager.getActiveDevicesForUser(userId, currentDeviceId);

        List<DeviceItemDto> deviceList = new ArrayList<>();
        for (com.atirath.trading.modules.auth.dto.device.ActiveDeviceSessionDto s : activeSessions) {
            deviceList.add(DeviceItemDto.builder()
                    .id(s.getDeviceId())
                    .name(s.getName())
                    .deviceType(s.getDeviceType())
                    .osBrowser(s.getOs())
                    .location(s.getLocation())
                    .ipAddress(s.getIpAddress())
                    .status(s.isCurrent() ? "Active Now" : "Active")
                    .isCurrentDevice(s.isCurrent())
                    .lastActive(s.getLastActive())
                    .canRevoke(!s.isCurrent())
                    .build());
        }

        return ActiveDevicesPageDto.builder()
                .memberId(sec.getMemberId())
                .fullName(sec.getFullName())
                .memberBadge(sec.getMemberBadge())
                .email(sec.getEmail())
                .mobileNumber(sec.getMobileNumber())
                .companyName(sec.getCompanyName())
                .designation(sec.getDesignation())
                .industry(sec.getIndustry())
                .memberSince(sec.getMemberSince())
                .avatarInitials(sec.getAvatarInitials())
                .devices(deviceList)
                .activeDevicesCount(deviceList.size())
                .maxDevicesLimit(2) // STRICT MAXIMUM = 2
                .securityTips(Arrays.asList(
                        "Strict limit of 2 active devices allowed across Web and Mobile combined.",
                        "If you log in on a 3rd device, you must log out of one of these 2 devices.",
                        "Logged out devices are immediately terminated for your security.",
                        "Never share your credentials with anyone."
                ))
                .recentActivity(sec.getLoginHistory())
                .build();
    }

    @Transactional
    public void revokeDevice(Long userId, String deviceId) {
        if ("dev-current".equals(deviceId)) {
            throw new ApiException("Cannot revoke the current active browser session", HttpStatus.BAD_REQUEST);
        }
        userDeviceSessionManager.disconnectDevice(userId, deviceId);
        terminateSession(userId, deviceId);
    }

    // =========================================================================
    // 6. Notifications
    // =========================================================================

    @Transactional(readOnly = true)
    public NotificationsPageDto getNotificationsPage(Long userId, String tab, String search, String type) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);

        List<AppNotification> dbNotifs = appNotificationRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(userId);
        List<NotificationItemDto> allItems = dbNotifs.stream()
                .map(n -> NotificationItemDto.builder()
                        .id(String.valueOf(n.getNotificationId()))
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getNotificationType() != null ? n.getNotificationType() : "SYSTEM")
                        .timeAgo(formatRelativeTime(n.getCreatedAt()))
                        .isUnread("UNREAD".equalsIgnoreCase(n.getStatus()))
                        .iconTheme(resolveIconTheme(n.getNotificationType()))
                        .build())
                .collect(Collectors.toList());

        List<NotificationItemDto> filtered = allItems.stream()
                .filter(n -> {
                    if ("UNREAD".equalsIgnoreCase(tab)) return Boolean.TRUE.equals(n.getIsUnread());
                    if ("MENTIONS".equalsIgnoreCase(tab)) return "NEGOTIATION".equalsIgnoreCase(n.getType());
                    if ("SYSTEM_UPDATES".equalsIgnoreCase(tab)) return "SYSTEM".equalsIgnoreCase(n.getType());
                    return true;
                })
                .filter(n -> {
                    if (isPresent(type) && !"ALL".equalsIgnoreCase(type)) {
                        return type.equalsIgnoreCase(n.getType());
                    }
                    return true;
                })
                .filter(n -> {
                    if (isPresent(search)) {
                        String q = search.toLowerCase(Locale.ENGLISH);
                        return n.getTitle().toLowerCase(Locale.ENGLISH).contains(q)
                                || n.getMessage().toLowerCase(Locale.ENGLISH).contains(q);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int unread = (int) allItems.stream().filter(n -> Boolean.TRUE.equals(n.getIsUnread())).count();

        NotificationPreferencesDto prefs = getOrInitNotificationPreferences(extras);
        NotificationChannelsDto channels = loadNotificationChannels(userId, extras);

        return NotificationsPageDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : "Member")
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .unreadCount(unread)
                .notifications(filtered)
                .preferences(prefs)
                .channels(channels)
                .build();
    }

    private NotificationPreferencesDto getOrInitNotificationPreferences(UserProfileExtras extras) {
        if (extras.notificationPreferences == null) {
            extras.notificationPreferences = NotificationPreferencesDto.builder()
                    .tradeNotifications(true)
                    .negotiationMessages(true)
                    .settlementUpdates(true)
                    .kycCompliance(true)
                    .memberRequests(true)
                    .systemUpdates(true)
                    .securityAlerts(true)
                    .marketingCommunications(false)
                    .build();
        }
        return extras.notificationPreferences;
    }

    private NotificationChannelsDto loadNotificationChannels(Long userId, UserProfileExtras extras) {
        if (extras.notificationChannels == null) {
            extras.notificationChannels = NotificationChannelsDto.builder()
                    .inApp(true)
                    .email(true)
                    .sms(false)
                    .build();
        }
        List<MemberNotificationPreference> dbPrefs = memberNotificationPreferenceRepository.findByUserAccount_UserId(userId);
        for (MemberNotificationPreference p : dbPrefs) {
            if ("IN_APP".equalsIgnoreCase(p.getModeOfNotification())) {
                extras.notificationChannels.setInApp(Boolean.TRUE.equals(p.getIsEnabled()));
            } else if ("EMAIL".equalsIgnoreCase(p.getModeOfNotification())) {
                extras.notificationChannels.setEmail(Boolean.TRUE.equals(p.getIsEnabled()));
            } else if ("SMS".equalsIgnoreCase(p.getModeOfNotification())) {
                extras.notificationChannels.setSms(Boolean.TRUE.equals(p.getIsEnabled()));
            }
        }
        return extras.notificationChannels;
    }

    private String formatRelativeTime(OffsetDateTime dt) {
        if (dt == null) return "-";
        Duration diff = Duration.between(dt, OffsetDateTime.now());
        long mins = diff.toMinutes();
        if (mins < 1) return "Just now";
        if (mins < 60) return mins + " mins ago";
        long hours = diff.toHours();
        if (hours < 24) return hours + " hours ago";
        long days = diff.toDays();
        return days + " days ago";
    }

    private String resolveIconTheme(String notifType) {
        if (notifType == null) return "blue";
        String upper = notifType.toUpperCase(Locale.ENGLISH);
        if (upper.contains("TRADE")) return "green";
        if (upper.contains("NEGOTIATION")) return "purple";
        if (upper.contains("SETTLEMENT") || upper.contains("ESCROW")) return "orange";
        if (upper.contains("KYC") || upper.contains("SECURITY")) return "red";
        return "blue";
    }

    @Transactional
    public void markAllNotificationsAsRead(Long userId) {
        loadUser(userId);
        List<AppNotification> all = appNotificationRepository.findByUserAccount_UserIdOrderByCreatedAtDesc(userId);
        OffsetDateTime now = OffsetDateTime.now();
        for (AppNotification n : all) {
            if (!"READ".equalsIgnoreCase(n.getStatus())) {
                n.setStatus("READ");
                n.setReadAt(now);
            }
        }
        appNotificationRepository.saveAll(all);
    }

    @Transactional
    public void markNotificationAsRead(Long userId, String id) {
        loadUser(userId);
        Long notifId = Long.parseLong(id);
        appNotificationRepository.findById(notifId)
                .filter(n -> Objects.equals(n.getUserAccount().getUserId(), userId))
                .ifPresent(n -> {
                    n.setStatus("READ");
                    n.setReadAt(OffsetDateTime.now());
                    appNotificationRepository.save(n);
                });
    }

    public NotificationPreferencesDto updateNotificationPreferences(Long userId, NotificationPreferencesDto update) {
        loadUser(userId);
        NotificationPreferencesDto prefs = getOrInitNotificationPreferences(getExtras(userId));
        if (update.getTradeNotifications() != null) prefs.setTradeNotifications(update.getTradeNotifications());
        if (update.getNegotiationMessages() != null) prefs.setNegotiationMessages(update.getNegotiationMessages());
        if (update.getSettlementUpdates() != null) prefs.setSettlementUpdates(update.getSettlementUpdates());
        if (update.getKycCompliance() != null) prefs.setKycCompliance(update.getKycCompliance());
        if (update.getMemberRequests() != null) prefs.setMemberRequests(update.getMemberRequests());
        if (update.getSystemUpdates() != null) prefs.setSystemUpdates(update.getSystemUpdates());
        if (update.getSecurityAlerts() != null) prefs.setSecurityAlerts(update.getSecurityAlerts());
        if (update.getMarketingCommunications() != null) prefs.setMarketingCommunications(update.getMarketingCommunications());
        return prefs;
    }

    @Transactional
    public NotificationChannelsDto updateNotificationChannels(Long userId, NotificationChannelsDto update) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        NotificationChannelsDto channels = loadNotificationChannels(userId, extras);
        if (update.getInApp() != null) {
            channels.setInApp(update.getInApp());
            saveChannelPreference(user, "IN_APP", update.getInApp());
        }
        if (update.getEmail() != null) {
            channels.setEmail(update.getEmail());
            saveChannelPreference(user, "EMAIL", update.getEmail());
        }
        if (update.getSms() != null) {
            channels.setSms(update.getSms());
            saveChannelPreference(user, "SMS", update.getSms());
        }
        return channels;
    }

    private void saveChannelPreference(UserAccount user, String mode, boolean enabled) {
        MemberNotificationPreference pref = memberNotificationPreferenceRepository
                .findByUserAccount_UserIdAndModeOfNotification(user.getUserId(), mode)
                .orElseGet(() -> MemberNotificationPreference.builder()
                        .userAccount(user)
                        .modeOfNotification(mode)
                        .build());
        pref.setIsEnabled(enabled);
        memberNotificationPreferenceRepository.save(pref);
    }

    // =========================================================================
    // 7. Documents
    // =========================================================================

    @Transactional(readOnly = true)
    public DocumentsPageDto getDocumentsPage(Long userId, String category, String search) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);

        List<AccountDocument> dbDocs = accountDocumentRepository.findByUserAccount_UserId(userId);
        boolean isVerified = user.getAccountStatus() == AccountStatus.ACTIVE;

        List<DocumentRecordDto> records = dbDocs.stream()
                .map(d -> DocumentRecordDto.builder()
                        .id(String.valueOf(d.getDocumentId()))
                        .name(isPresent(d.getDocumentName()) ? d.getDocumentName() : d.getDocumentType().name())
                        .fileName(extractFileName(d.getFilePath()))
                        .category(mapDocCategory(d.getDocumentType()))
                        .uploadedOn(formatDate(d.getUploadedAt()))
                        .expiryDate("-")
                        .status(isVerified ? "Verified" : "Under Review")
                        .fileUrl(d.getFilePath())
                        .build())
                .collect(Collectors.toList());

        long totalBytes = dbDocs.stream()
                .mapToLong(d -> d.getFileSizeBytes() != null ? d.getFileSizeBytes() : 0L)
                .sum();
        double totalQuotaMb = 25.0;
        double usedMb = totalBytes / (1024.0 * 1024.0);
        int pct = (int) Math.min(100, Math.round((usedMb / totalQuotaMb) * 100.0));
        double availMb = Math.max(0.0, totalQuotaMb - usedMb);

        StorageUsageDto storageUsage = StorageUsageDto.builder()
                .usedPercentage(pct)
                .usedMb(String.format(Locale.ENGLISH, "%.2f MB", usedMb))
                .totalMb(String.format(Locale.ENGLISH, "%.1f MB", totalQuotaMb))
                .availableMb(String.format(Locale.ENGLISH, "%.2f MB Available", availMb))
                .build();

        List<DocumentRecordDto> filtered = records.stream()
                .filter(d -> {
                    if (isPresent(category) && !"ALL".equalsIgnoreCase(category)) {
                        return category.equalsIgnoreCase(d.getCategory());
                    }
                    return true;
                })
                .filter(d -> {
                    if (isPresent(search)) {
                        String q = search.toLowerCase(Locale.ENGLISH);
                        return d.getName().toLowerCase(Locale.ENGLISH).contains(q)
                                || d.getFileName().toLowerCase(Locale.ENGLISH).contains(q);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<ExpiryAlertDto> expiryAlerts = new ArrayList<>();
        if (dbDocs.isEmpty()) {
            expiryAlerts.add(ExpiryAlertDto.builder()
                    .name("No Documents Uploaded")
                    .alertText("Upload identity or bank documents to complete compliance")
                    .expiryDate("-")
                    .theme("yellow")
                    .build());
        }

        return DocumentsPageDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : "Member")
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .documents(filtered)
                .storageUsage(storageUsage)
                .documentGuidelines(Arrays.asList(
                        "Supported formats: PDF, JPG, PNG (Max 5 MB).",
                        "Ensure documents are clear and legible.",
                        "File size should not exceed 5 MB per document.",
                        "Documents may be subject to verification.",
                        "Keep your documents updated, especially those with expiry dates."
                ))
                .expiryAlerts(expiryAlerts)
                .build();
    }

    private String extractFileName(String path) {
        if (!isPresent(path)) return "document.pdf";
        int idx = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return idx >= 0 && idx + 1 < path.length() ? path.substring(idx + 1) : path;
    }

    private String mapDocCategory(DocumentType type) {
        if (type == null) return "Other";
        return switch (type) {
            case IDENTITY_PROOF, ADDRESS_PROOF -> "Identity";
            case BANK_PASSBOOK -> "Bank";
            case LIVE_PHOTO -> "Other";
        };
    }

    @Transactional
    public DocumentRecordDto uploadDocument(Long userId, String name, String category, String fileName, String fileUrl) {
        UserAccount user = loadUser(userId);
        DocumentType docType = switch (category != null ? category.trim().toLowerCase(Locale.ENGLISH) : "") {
            case "identity" -> DocumentType.IDENTITY_PROOF;
            case "organization" -> DocumentType.ADDRESS_PROOF;
            case "bank" -> DocumentType.BANK_PASSBOOK;
            default -> DocumentType.LIVE_PHOTO;
        };

        String resolvedName = isPresent(name) ? name.trim() : "Uploaded Document";
        String resolvedFileName = isPresent(fileName) ? fileName.trim() : "document.pdf";
        String resolvedPath = isPresent(fileUrl) ? fileUrl.trim() : "/uploads/user-" + userId + "/" + resolvedFileName;

        AccountDocument saved = AccountDocument.builder()
                .userAccount(user)
                .documentType(docType)
                .documentName(resolvedName)
                .filePath(resolvedPath)
                .mimeType(resolvedFileName.toLowerCase(Locale.ENGLISH).endsWith(".png") ? "image/png" : "application/pdf")
                .fileSizeBytes(204800L)
                .build();
        AccountDocument entity = accountDocumentRepository.save(saved);

        return DocumentRecordDto.builder()
                .id(String.valueOf(entity.getDocumentId()))
                .name(entity.getDocumentName())
                .fileName(resolvedFileName)
                .category(isPresent(category) ? category.trim() : mapDocCategory(docType))
                .uploadedOn(formatDate(entity.getUploadedAt()))
                .expiryDate("-")
                .status(user.getAccountStatus() == AccountStatus.ACTIVE ? "Verified" : "Under Review")
                .fileUrl(entity.getFilePath())
                .build();
    }

    // =========================================================================
    // 8. Platform Settings
    // =========================================================================

    @Transactional(readOnly = true)
    public PlatformSettingsPageDto getPlatformSettingsPage(Long userId) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        String fullName = portfolioDataHelper.resolveUserFullName(user);
        CorporateProfile corp = corporateProfileRepository.findByUserAccount_UserId(userId).orElse(null);

        GeneralSettingsDto general = GeneralSettingsDto.builder()
                .emailAddress(user.getEmail())
                .emailStatus(user.getAccountStatus() == AccountStatus.ACTIVE ? "Verified" : "Pending")
                .mobileNumber(user.getMobileNumber())
                .mobileStatus(isPresent(user.getMobileNumber()) ? "Verified" : "Pending")
                .company(resolveCompanyName(user, corp))
                .designation(isPresent(extras.designation) ? extras.designation : "Member")
                .build();

        if (extras.platformPreferences == null) {
            NotificationChannelsDto channels = loadNotificationChannels(userId, extras);
            extras.platformPreferences = PlatformPreferencesDto.builder()
                    .receiveEmailNotifications(Boolean.TRUE.equals(channels.getEmail()))
                    .receiveSmsNotifications(Boolean.TRUE.equals(channels.getSms()))
                    .marketingCommunications(false)
                    .showPortfolioSummaryOnLogin(true)
                    .autoLogout("15 mins")
                    .build();
        }

        return PlatformSettingsPageDto.builder()
                .memberId(resolveMemberCode(user))
                .fullName(fullName)
                .memberBadge(resolveMemberBadge(user))
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .companyName(general.getCompany())
                .designation(general.getDesignation())
                .industry(isPresent(extras.companyIndustry) ? extras.companyIndustry : "Private Equity")
                .memberSince(formatDate(user.getCreatedAt()))
                .avatarInitials(computeInitials(fullName))
                .general(general)
                .platformPreferences(extras.platformPreferences)
                .appearance(extras.appearance)
                .languageRegion(extras.languageRegion)
                .build();
    }

    @Transactional
    public GeneralSettingsDto updateGeneralSettings(Long userId, GeneralSettingsDto update) {
        UserAccount user = loadUser(userId);
        UserProfileExtras extras = getExtras(userId);

        if (isPresent(update.getEmailAddress()) && !update.getEmailAddress().trim().equalsIgnoreCase(user.getEmail())) {
            String newEmail = update.getEmailAddress().trim();
            if (userAccountRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new ApiException("Email address is already in use", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(newEmail);
            userAccountRepository.save(user);
        }
        if (isPresent(update.getMobileNumber()) && !update.getMobileNumber().trim().equals(user.getMobileNumber())) {
            String newMobile = update.getMobileNumber().trim();
            if (userAccountRepository.existsByMobileNumber(newMobile)) {
                throw new ApiException("Mobile number is already in use", HttpStatus.BAD_REQUEST);
            }
            user.setMobileNumber(newMobile);
            userAccountRepository.save(user);
        }
        if (isPresent(update.getCompany())) {
            corporateProfileRepository.findByUserAccount_UserId(userId).ifPresent(corp -> {
                corp.setCompanyName(update.getCompany().trim());
                corporateProfileRepository.save(corp);
            });
        }
        if (update.getDesignation() != null) {
            extras.designation = update.getDesignation().trim();
        }
        return getPlatformSettingsPage(userId).getGeneral();
    }

    @Transactional
    public PlatformPreferencesDto updatePlatformPreferences(Long userId, PlatformPreferencesDto update) {
        UserAccount user = loadUser(userId);
        PlatformPreferencesDto prefs = getPlatformSettingsPage(userId).getPlatformPreferences();
        if (update.getReceiveEmailNotifications() != null) {
            prefs.setReceiveEmailNotifications(update.getReceiveEmailNotifications());
            saveChannelPreference(user, "EMAIL", update.getReceiveEmailNotifications());
        }
        if (update.getReceiveSmsNotifications() != null) {
            prefs.setReceiveSmsNotifications(update.getReceiveSmsNotifications());
            saveChannelPreference(user, "SMS", update.getReceiveSmsNotifications());
        }
        if (update.getMarketingCommunications() != null) prefs.setMarketingCommunications(update.getMarketingCommunications());
        if (update.getShowPortfolioSummaryOnLogin() != null) prefs.setShowPortfolioSummaryOnLogin(update.getShowPortfolioSummaryOnLogin());
        if (update.getAutoLogout() != null) prefs.setAutoLogout(update.getAutoLogout());
        return prefs;
    }

    public AppearanceSettingsDto updateAppearance(Long userId, AppearanceSettingsDto update) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (update.getSelectedTheme() != null) extras.appearance.setSelectedTheme(update.getSelectedTheme());
        return extras.appearance;
    }

    public LanguageRegionDto updateLanguageRegion(Long userId, LanguageRegionDto update) {
        loadUser(userId);
        UserProfileExtras extras = getExtras(userId);
        if (update.getLanguage() != null) extras.languageRegion.setLanguage(update.getLanguage());
        if (update.getTimezone() != null) extras.languageRegion.setTimezone(update.getTimezone());
        return extras.languageRegion;
    }

    @Transactional(readOnly = true)
    public Map<String, String> exportAccountData(Long userId) {
        UserAccount user = loadUser(userId);
        String code = resolveMemberCode(user);
        return Map.of(
                "downloadUrl", "/api/v1/portfolio/download/report",
                "memberId", code,
                "status", "READY"
        );
    }

    @Transactional(readOnly = true)
    public Map<String, String> downloadStatements(Long userId) {
        UserAccount user = loadUser(userId);
        String code = resolveMemberCode(user);
        return Map.of(
                "downloadUrl", "/api/v1/portfolio/download/statement",
                "memberId", code,
                "status", "READY"
        );
    }
}
