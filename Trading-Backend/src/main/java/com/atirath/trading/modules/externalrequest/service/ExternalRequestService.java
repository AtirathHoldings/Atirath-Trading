package com.atirath.trading.modules.externalrequest.service;

import com.atirath.trading.modules.externalrequest.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExternalRequestService {

    private final Map<String, ExternalAccessRequestDto> requestStore = new ConcurrentHashMap<>();
    private final Map<String, List<KycDocumentDto>> kycDocStore = new ConcurrentHashMap<>();
    private final Map<String, List<ChecklistItemDto>> checklistStore = new ConcurrentHashMap<>();
    private final Map<String, List<VerificationHistoryItemDto>> historyStore = new ConcurrentHashMap<>();
    private final Map<String, List<ActivityDto>> activityStore = new ConcurrentHashMap<>();
    private final AtomicLong requestIdCounter = new AtomicLong(47);
    private final AtomicLong noteIdCounter = new AtomicLong(10);
    private final AtomicLong docIdCounter = new AtomicLong(200);

    private static final DateTimeFormatter DISPLAY_DT_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @PostConstruct
    public void init() {
        seedSampleData();
        log.info("ExternalRequestService initialized with pre-loaded mock requests matching UI designs.");
    }

    private void seedSampleData() {
        // --- REQUEST 1: REQ-2026-00045 (Rohit Sharma - GreenTech Solutions Pvt Ltd) ---
        String reqId1 = "REQ-2026-00045";
        List<String> reqAccess1 = new ArrayList<>(Arrays.asList("View Company Information", "Buy Shares", "Sell Shares", "Negotiation", "Participate in Private Placements"));
        List<String> appAccess1 = new ArrayList<>(Arrays.asList("View Company Information", "Buy Shares", "Sell Shares"));

        List<NoteDto> notes1 = new ArrayList<>();
        notes1.add(NoteDto.builder()
                .id(1L)
                .authorName("Anitha Verma")
                .authorRole("Compliance")
                .initials("AV")
                .timestamp("16 Sep 2026, 09:30 AM")
                .noteText("Initial verification completed. KYC documents pending.")
                .build());
        notes1.add(NoteDto.builder()
                .id(2L)
                .authorName("Suresh Kumar")
                .authorRole("Operations")
                .initials("SK")
                .timestamp("15 Sep 2026, 02:15 PM")
                .noteText("Applicant details verified. Company is valid and active.")
                .build());

        List<TimelineStepDto> timeline1 = new ArrayList<>();
        timeline1.add(TimelineStepDto.builder()
                .stepNumber(1)
                .title("Request Submitted")
                .description("Applicant submitted the access request.")
                .timestamp("15 Sep 2026, 10:24 AM")
                .actor("Rohit Sharma")
                .state("COMPLETED")
                .build());
        timeline1.add(TimelineStepDto.builder()
                .stepNumber(2)
                .title("Under Review")
                .description("Application is under initial review.")
                .timestamp("16 Sep 2026, 09:30 AM")
                .actor("Anitha Verma (Compliance)")
                .state("IN_PROGRESS")
                .build());
        timeline1.add(TimelineStepDto.builder()
                .stepNumber(3)
                .title("KYC Pending")
                .description("Waiting for KYC document verification.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());
        timeline1.add(TimelineStepDto.builder()
                .stepNumber(4)
                .title("Verification")
                .description("Final verification and compliance checks.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());
        timeline1.add(TimelineStepDto.builder()
                .stepNumber(5)
                .title("Approved / Rejected")
                .description("Access will be granted after approval.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());

        ExternalAccessRequestDto req1 = ExternalAccessRequestDto.builder()
                .requestId(reqId1)
                .status("UNDER_REVIEW")
                .statusBadge("Under Review")
                .source("Web Application")
                .submittedOn("15 Sep 2026, 10:24 AM")
                .lastUpdated("16 Sep 2026, 09:30 AM")
                .fullName("Rohit Sharma")
                .dateOfBirth("12 Mar 1995")
                .email("rohit.sharma@example.com")
                .mobileNumber("+91 98765 43210")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("1234 5678 9012")
                .address("Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081 India")
                .nationality("Indian")
                .occupation("Business Professional")
                .experience("8+ Years")
                .linkedInProfile("https://linkedin.com/in/rohitsharma")
                .avatarInitials("RS")
                .companyName("GreenTech Solutions Pvt Ltd")
                .industry("Renewable Energy")
                .companyType("Private Limited")
                .designation("Director")
                .companyPan("AABCG1234H")
                .registrationNumber("U40100TG2020PTC123456")
                .companyAddress("Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
                .companyWebsite("https://www.greentechsolutions.com")
                .companyLogo("GreenTech Solutions Renewable Energy for a Better Tomorrow")
                .purposeOfRequest("To invest in ATIRATH Group renewable energy companies for strategic partnership and long-term collaboration.")
                .requestedAccess(reqAccess1)
                .expectedInvestmentRange("₹10,00,000 - ₹50,00,000")
                .investmentPurpose("Strategic Investment")
                .sourceOfFunds("Business Profits")
                .annualIncomeRange("₹25,00,000 - ₹50,00,000")
                .approvedOn("16 Sep 2026, 11:20 AM")
                .approvedAccess(appAccess1)
                .accessValidFrom("16 Sep 2026")
                .accessValidTill("15 Sep 2027 (1 Year)")
                .approvedBy("Compliance Team")
                .welcomeMessage("Welcome to ATIRATH. You can now access the internal market and trade in ATIRATH Group companies as per the approved access level.")
                .notes(notes1)
                .timeline(timeline1)
                .build();
        requestStore.put(reqId1, req1);

        // Seed KYC Documents for REQ 1
        List<KycDocumentDto> docs1 = new ArrayList<>();
        docs1.add(KycDocumentDto.builder()
                .id(101L)
                .category("IDENTITY_DOCUMENT")
                .documentType("PAN Card")
                .documentNumber("ABCDE1234F")
                .holderName("Rohit Sharma")
                .status("Verified")
                .verifiedOn("15 Sep 2026, 02:15 PM")
                .verifiedBy("Anitha Verma (Compliance)")
                .fileUrl("https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=600&q=80")
                .build());
        docs1.add(KycDocumentDto.builder()
                .id(102L)
                .category("IDENTITY_DOCUMENT")
                .documentType("Aadhaar Card")
                .documentNumber("1234 5678 9012")
                .holderName("Rohit Sharma")
                .status("Under Review")
                .submittedOn("15 Sep 2026, 10:30 AM")
                .remarks("Document clear. Waiting for UIDAI verification.")
                .fileUrl("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?auto=format&fit=crop&w=600&q=80")
                .build());
        docs1.add(KycDocumentDto.builder()
                .id(103L)
                .category("ADDRESS_PROOF")
                .documentType("Electricity Bill")
                .address("Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
                .issueDate("01 Sep 2026")
                .status("Not Verified")
                .remarks("Document uploaded. Verification pending.")
                .fileUrl("https://images.unsplash.com/photo-1568602471122-7832951cc4c5?auto=format&fit=crop&w=600&q=80")
                .build());
        docs1.add(KycDocumentDto.builder()
                .id(104L)
                .category("ORGANIZATION_DOCUMENT")
                .documentType("Certificate of Incorporation")
                .companyName("GreenTech Solutions Pvt Ltd")
                .registrationNumber("U40100TG2020PTC123456")
                .issueDate("12 Jan 2020")
                .status("Verified")
                .remarks("Valid document.")
                .fileUrl("https://images.unsplash.com/photo-1450133064473-71024230f91b?auto=format&fit=crop&w=600&q=80")
                .build());
        kycDocStore.put(reqId1, docs1);

        // Seed Compliance Checklist
        List<ChecklistItemDto> checklist1 = new ArrayList<>();
        checklist1.add(ChecklistItemDto.builder().name("Identity Proof (PAN)").status("Verified").isSatisfied(true).build());
        checklist1.add(ChecklistItemDto.builder().name("Identity Proof (Aadhaar)").status("Under Review").isSatisfied(false).build());
        checklist1.add(ChecklistItemDto.builder().name("Address Proof").status("Not Uploaded").isSatisfied(false).build());
        checklist1.add(ChecklistItemDto.builder().name("Organization Documents").status("Not Verified").isSatisfied(false).build());
        checklist1.add(ChecklistItemDto.builder().name("Additional Documents").status("Optional").isSatisfied(true).build());
        checklistStore.put(reqId1, checklist1);

        // Seed History
        List<VerificationHistoryItemDto> hist1 = new ArrayList<>();
        hist1.add(VerificationHistoryItemDto.builder().title("PAN Verified").timestamp("15 Sep 2026, 02:15 PM").actor("Anitha Verma").status("Verified").build());
        hist1.add(VerificationHistoryItemDto.builder().title("Aadhaar Submitted").timestamp("15 Sep 2026, 10:30 AM").actor("Rohit Sharma").status("Submitted").build());
        hist1.add(VerificationHistoryItemDto.builder().title("Address Proof Pending").timestamp("-").actor("-").status("Pending").build());
        hist1.add(VerificationHistoryItemDto.builder().title("KYC Under Review").timestamp("16 Sep 2026, 09:30 AM").actor("Anitha Verma").status("In Progress").build());
        historyStore.put(reqId1, hist1);

        // Seed Activities
        List<ActivityDto> act1 = new ArrayList<>();
        act1.add(ActivityDto.builder().actorName("Anitha Verma").actorRole("Compliance").initials("AV").timestamp("16 Sep 2026, 09:30 AM").message("Application moved to KYC verification. Documents are under review.").build());
        act1.add(ActivityDto.builder().actorName("Suresh Kumar").actorRole("Operations").initials("SK").timestamp("15 Sep 2026, 02:15 PM").message("Initial review completed. KYC documents required.").build());
        act1.add(ActivityDto.builder().actorName("System").actorRole("System").initials("SYS").timestamp("15 Sep 2026, 10:24 AM").message("Request submitted successfully.").build());
        act1.add(ActivityDto.builder().actorName("Rohit Sharma").actorRole("Applicant").initials("RS").timestamp("15 Sep 2026, 10:24 AM").message("External access request submitted.").build());
        activityStore.put(reqId1, act1);

        // --- REQUEST 2: REQ-2026-00046 (Priya Mehta - Sunrise Ventures LLP - Rejected) ---
        String reqId2 = "REQ-2026-00046";
        List<String> reqAccess2 = new ArrayList<>(Arrays.asList("View Company Information", "Buy Shares", "Sell Shares"));
        List<String> whatToDo2 = new ArrayList<>(Arrays.asList(
                "Review the reason for rejection.",
                "Update the required documents and submit a new request.",
                "For further assistance, contact our support team."
        ));

        ExternalAccessRequestDto req2 = ExternalAccessRequestDto.builder()
                .requestId(reqId2)
                .status("REJECTED")
                .statusBadge("Rejected")
                .source("Web Application")
                .submittedOn("14 Sep 2026, 11:00 AM")
                .lastUpdated("16 Sep 2026, 04:15 PM")
                .fullName("Priya Mehta")
                .dateOfBirth("24 Aug 1991")
                .email("priya.mehta@sunriseventures.com")
                .mobileNumber("+91 98111 22334")
                .panNumber("BCDEF2345G")
                .aadhaarNumber("2345 6789 0123")
                .address("Suite 102, Sunrise Towers, Bandra Kurla Complex, Mumbai - 400051")
                .nationality("Indian")
                .occupation("Venture Capitalist")
                .experience("10+ Years")
                .avatarInitials("PM")
                .companyName("Sunrise Ventures LLP")
                .industry("Financial Services & VC")
                .companyType("Limited Liability Partnership")
                .designation("Managing Partner")
                .companyPan("BBCDE3344F")
                .registrationNumber("AAH-1234")
                .companyAddress("Sunrise Towers, BKC, Mumbai - 400051")
                .companyWebsite("https://www.sunriseventures.com")
                .purposeOfRequest("Direct institutional secondary acquisition in tech subsidiaries.")
                .requestedAccess(reqAccess2)
                .rejectedOn("16 Sep 2026, 04:15 PM")
                .rejectedBy("Anitha Verma (Compliance)")
                .reasonForRejection("The submitted KYC documents are incomplete. Please provide a valid address proof and updated organization registration certificate.")
                .whatYouCanDo(whatToDo2)
                .build();
        requestStore.put(reqId2, req2);

        // --- REQUEST 3: REQ-2026-00047 (Vikram Patel - BlueSky Innovations - Submitted) ---
        String reqId3 = "REQ-2026-00047";
        ExternalAccessRequestDto req3 = ExternalAccessRequestDto.builder()
                .requestId(reqId3)
                .status("SUBMITTED")
                .statusBadge("Submitted")
                .source("Web Application")
                .submittedOn("17 Sep 2026, 02:40 PM")
                .lastUpdated("17 Sep 2026, 02:40 PM")
                .fullName("Vikram Patel")
                .dateOfBirth("05 Jan 1988")
                .email("vikram.patel@blueskyinnovations.in")
                .mobileNumber("+91 98222 33445")
                .panNumber("CDEFG3456H")
                .aadhaarNumber("3456 7890 1234")
                .address("502, Infotech City, Bengaluru, Karnataka - 560100")
                .nationality("Indian")
                .occupation("Investor / Founder")
                .experience("12+ Years")
                .avatarInitials("VP")
                .companyName("BlueSky Innovations Pvt Ltd")
                .industry("Information Technology")
                .companyType("Private Limited")
                .designation("Founder & CEO")
                .companyPan("CCDDE5566K")
                .registrationNumber("U72200KA2018PTC112233")
                .companyAddress("502, Infotech City, Bengaluru, Karnataka - 560100")
                .companyWebsite("https://www.blueskyinnovations.in")
                .purposeOfRequest("Strategic investment in renewable energy and green fintech.")
                .requestedAccess(new ArrayList<>(Arrays.asList("View Company Information", "Buy Shares")))
                .build();
        requestStore.put(reqId3, req3);
    }

    // --- List & Search Requests ---
    public List<ExternalRequestListItemDto> getAllRequests(String search, String status) {
        return requestStore.values().stream()
                .filter(r -> {
                    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                        if (!r.getStatus().equalsIgnoreCase(status.trim()) &&
                            !r.getStatusBadge().equalsIgnoreCase(status.trim())) {
                            return false;
                        }
                    }
                    if (search != null && !search.trim().isEmpty()) {
                        String query = search.toLowerCase().trim();
                        boolean matchesName = r.getFullName() != null && r.getFullName().toLowerCase().contains(query);
                        boolean matchesCompany = r.getCompanyName() != null && r.getCompanyName().toLowerCase().contains(query);
                        boolean matchesEmail = r.getEmail() != null && r.getEmail().toLowerCase().contains(query);
                        boolean matchesReqId = r.getRequestId() != null && r.getRequestId().toLowerCase().contains(query);
                        return matchesName || matchesCompany || matchesEmail || matchesReqId;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getRequestId().compareTo(a.getRequestId()))
                .map(this::mapToListItem)
                .collect(Collectors.toList());
    }

    private ExternalRequestListItemDto mapToListItem(ExternalAccessRequestDto r) {
        return ExternalRequestListItemDto.builder()
                .requestId(r.getRequestId())
                .fullName(r.getFullName())
                .email(r.getEmail())
                .mobileNumber(r.getMobileNumber())
                .companyName(r.getCompanyName())
                .designation(r.getDesignation())
                .industry(r.getIndustry())
                .status(r.getStatus())
                .statusBadge(r.getStatusBadge())
                .submittedOn(r.getSubmittedOn())
                .lastUpdated(r.getLastUpdated())
                .avatarInitials(r.getAvatarInitials())
                .requestedAccess(r.getRequestedAccess())
                .build();
    }

    // --- Get Single Request Detail ---
    public ExternalAccessRequestDto getRequestById(String requestId) {
        ExternalAccessRequestDto dto = requestStore.get(requestId);
        if (dto == null) {
            throw new NoSuchElementException("External Member Request not found: " + requestId);
        }
        return dto;
    }

    // --- Create / Submit New Request ---
    public ExternalAccessRequestDto submitRequest(SubmitExternalAccessRequestDto input, boolean isDraft) {
        String newId = "REQ-2026-" + String.format("%05d", requestIdCounter.incrementAndGet());
        String nowFormatted = LocalDateTime.now().format(DISPLAY_DT_FMT);

        String initials = "EM";
        if (input.getFullName() != null && !input.getFullName().trim().isEmpty()) {
            String[] parts = input.getFullName().trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase();
            }
        }

        String status = isDraft ? "DRAFT" : "SUBMITTED";
        String statusBadge = isDraft ? "Draft" : "Submitted";

        List<TimelineStepDto> timeline = new ArrayList<>();
        timeline.add(TimelineStepDto.builder()
                .stepNumber(1)
                .title("Request Submitted")
                .description(isDraft ? "Request saved as draft" : "Applicant submitted the access request.")
                .timestamp(nowFormatted)
                .actor(input.getFullName())
                .state(isDraft ? "IN_PROGRESS" : "COMPLETED")
                .build());
        timeline.add(TimelineStepDto.builder()
                .stepNumber(2)
                .title("Under Review")
                .description("Application awaiting review.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());
        timeline.add(TimelineStepDto.builder()
                .stepNumber(3)
                .title("KYC Pending")
                .description("Document verification pending.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());
        timeline.add(TimelineStepDto.builder()
                .stepNumber(4)
                .title("Verification")
                .description("Final compliance verification.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());
        timeline.add(TimelineStepDto.builder()
                .stepNumber(5)
                .title("Approved / Rejected")
                .description("Decision decision pending.")
                .timestamp("-")
                .actor("-")
                .state("PENDING")
                .build());

        ExternalAccessRequestDto request = ExternalAccessRequestDto.builder()
                .requestId(newId)
                .status(status)
                .statusBadge(statusBadge)
                .source("Web Application")
                .submittedOn(nowFormatted)
                .lastUpdated(nowFormatted)
                .fullName(input.getFullName())
                .dateOfBirth(input.getDateOfBirth())
                .email(input.getEmail())
                .mobileNumber(input.getMobileNumber())
                .panNumber(input.getPanNumber())
                .aadhaarNumber(input.getAadhaarNumber())
                .address(input.getAddress())
                .nationality(input.getNationality() != null ? input.getNationality() : "Indian")
                .occupation(input.getOccupation() != null ? input.getOccupation() : "Professional")
                .experience(input.getExperience() != null ? input.getExperience() : "5+ Years")
                .linkedInProfile(input.getLinkedInProfile())
                .avatarInitials(initials)
                .companyName(input.getCompanyName())
                .industry(input.getIndustry())
                .companyType(input.getCompanyType() != null ? input.getCompanyType() : "Private Limited")
                .designation(input.getDesignation())
                .companyPan(input.getCompanyPan())
                .registrationNumber(input.getRegistrationNumber())
                .companyAddress(input.getCompanyAddress())
                .companyWebsite(input.getCompanyWebsite())
                .purposeOfRequest(input.getPurposeOfRequest())
                .requestedAccess(input.getRequestedAccess() != null ? input.getRequestedAccess() : new ArrayList<>())
                .expectedInvestmentRange(input.getExpectedInvestmentRange())
                .investmentPurpose(input.getInvestmentPurpose())
                .sourceOfFunds(input.getSourceOfFunds())
                .annualIncomeRange(input.getAnnualIncomeRange())
                .timeline(timeline)
                .notes(new ArrayList<>())
                .build();

        requestStore.put(newId, request);

        // Auto-seed default KYC docs for verification
        List<KycDocumentDto> docs = new ArrayList<>();
        if (input.getPanNumber() != null) {
            docs.add(KycDocumentDto.builder()
                    .id(docIdCounter.incrementAndGet())
                    .category("IDENTITY_DOCUMENT")
                    .documentType("PAN Card")
                    .documentNumber(input.getPanNumber())
                    .holderName(input.getFullName())
                    .status("Under Review")
                    .submittedOn(nowFormatted)
                    .build());
        }
        if (input.getAadhaarNumber() != null) {
            docs.add(KycDocumentDto.builder()
                    .id(docIdCounter.incrementAndGet())
                    .category("IDENTITY_DOCUMENT")
                    .documentType("Aadhaar Card")
                    .documentNumber(input.getAadhaarNumber())
                    .holderName(input.getFullName())
                    .status("Under Review")
                    .submittedOn(nowFormatted)
                    .build());
        }
        if (input.getCompanyName() != null) {
            docs.add(KycDocumentDto.builder()
                    .id(docIdCounter.incrementAndGet())
                    .category("ORGANIZATION_DOCUMENT")
                    .documentType("Certificate of Incorporation")
                    .companyName(input.getCompanyName())
                    .registrationNumber(input.getRegistrationNumber())
                    .status("Under Review")
                    .submittedOn(nowFormatted)
                    .build());
        }
        kycDocStore.put(newId, docs);

        // Checklist
        List<ChecklistItemDto> checklist = new ArrayList<>();
        checklist.add(ChecklistItemDto.builder().name("Identity Proof (PAN)").status("Under Review").isSatisfied(false).build());
        checklist.add(ChecklistItemDto.builder().name("Identity Proof (Aadhaar)").status("Under Review").isSatisfied(false).build());
        checklist.add(ChecklistItemDto.builder().name("Address Proof").status("Not Uploaded").isSatisfied(false).build());
        checklist.add(ChecklistItemDto.builder().name("Organization Documents").status("Under Review").isSatisfied(false).build());
        checklist.add(ChecklistItemDto.builder().name("Additional Documents").status("Optional").isSatisfied(true).build());
        checklistStore.put(newId, checklist);

        // History
        List<VerificationHistoryItemDto> history = new ArrayList<>();
        history.add(VerificationHistoryItemDto.builder()
                .title("Request Submitted")
                .timestamp(nowFormatted)
                .actor(input.getFullName())
                .status("Submitted")
                .build());
        historyStore.put(newId, history);

        // Activities
        List<ActivityDto> activities = new ArrayList<>();
        activities.add(ActivityDto.builder()
                .actorName("System")
                .actorRole("System")
                .initials("SYS")
                .timestamp(nowFormatted)
                .message("Request " + newId + " submitted successfully.")
                .build());
        activities.add(ActivityDto.builder()
                .actorName(input.getFullName())
                .actorRole("Applicant")
                .initials(initials)
                .timestamp(nowFormatted)
                .message("External access request submitted.")
                .build());
        activityStore.put(newId, activities);

        return request;
    }

    // --- KYC Review Screen Details (Image 3) ---
    public KycReviewDto getKycReview(String requestId) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        List<KycDocumentDto> docs = kycDocStore.getOrDefault(requestId, new ArrayList<>());

        List<KycDocumentDto> identityDocs = docs.stream()
                .filter(d -> "IDENTITY_DOCUMENT".equalsIgnoreCase(d.getCategory()) || "IDENTITY_DOCUMENTS".equalsIgnoreCase(d.getCategory()))
                .collect(Collectors.toList());

        List<KycDocumentDto> addressProofs = docs.stream()
                .filter(d -> "ADDRESS_PROOF".equalsIgnoreCase(d.getCategory()))
                .collect(Collectors.toList());

        List<KycDocumentDto> orgDocs = docs.stream()
                .filter(d -> "ORGANIZATION_DOCUMENT".equalsIgnoreCase(d.getCategory()) || "ORGANIZATION_DOCUMENTS".equalsIgnoreCase(d.getCategory()))
                .collect(Collectors.toList());

        int verifiedCount = (int) docs.stream().filter(d -> "Verified".equalsIgnoreCase(d.getStatus())).count();
        int underReviewCount = (int) docs.stream().filter(d -> "Under Review".equalsIgnoreCase(d.getStatus())).count();
        int notVerifiedCount = (int) docs.stream().filter(d -> "Not Verified".equalsIgnoreCase(d.getStatus())).count();
        int total = docs.size();
        int completion = total > 0 ? (verifiedCount * 100 / total) : 0;

        return KycReviewDto.builder()
                .requestId(req.getRequestId())
                .fullName(req.getFullName())
                .avatarInitials(req.getAvatarInitials())
                .email(req.getEmail())
                .mobileNumber(req.getMobileNumber())
                .companyName(req.getCompanyName())
                .designation(req.getDesignation())
                .industry(req.getIndustry())
                .statusBadge("KYC Under Review")
                .submittedOn(req.getSubmittedOn())
                .lastUpdated(req.getLastUpdated())
                .completionPercentage(completion)
                .verifiedCount(verifiedCount)
                .underReviewCount(underReviewCount)
                .notVerifiedCount(notVerifiedCount)
                .optionalCount(0)
                .identityDocuments(identityDocs)
                .addressProof(addressProofs)
                .organizationDocuments(orgDocs)
                .complianceChecklist(checklistStore.getOrDefault(requestId, new ArrayList<>()))
                .verificationNotes(req.getNotes())
                .verificationHistory(historyStore.getOrDefault(requestId, new ArrayList<>()))
                .build();
    }

    // --- Update KYC Document Status ---
    public KycDocumentDto updateKycDocumentStatus(String requestId, Long docId, UpdateDocumentStatusDto update) {
        List<KycDocumentDto> docs = kycDocStore.get(requestId);
        if (docs == null) {
            throw new NoSuchElementException("No documents found for request " + requestId);
        }

        KycDocumentDto doc = docs.stream()
                .filter(d -> d.getId().equals(docId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID " + docId));

        doc.setStatus(update.getStatus());
        if (update.getRemarks() != null) {
            doc.setRemarks(update.getRemarks());
        }
        if (update.getVerifiedBy() != null) {
            doc.setVerifiedBy(update.getVerifiedBy());
        }
        String now = LocalDateTime.now().format(DISPLAY_DT_FMT);
        doc.setVerifiedOn(now);

        // Update history
        List<VerificationHistoryItemDto> history = historyStore.computeIfAbsent(requestId, k -> new ArrayList<>());
        history.add(0, VerificationHistoryItemDto.builder()
                .title(doc.getDocumentType() + " " + update.getStatus())
                .timestamp(now)
                .actor(update.getVerifiedBy() != null ? update.getVerifiedBy() : "Compliance Officer")
                .status(update.getStatus())
                .build());

        return doc;
    }

    // --- Request Status Tracking Details (Image 4) ---
    public RequestStatusDto getRequestStatus(String requestId) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        List<KycDocumentDto> docs = kycDocStore.getOrDefault(requestId, new ArrayList<>());

        List<RequiredDocumentDto> reqDocs = new ArrayList<>();
        reqDocs.add(createReqDoc("PAN Card", docs, false));
        reqDocs.add(createReqDoc("Aadhaar Card", docs, false));
        reqDocs.add(createReqDoc("Address Proof", docs, false));
        reqDocs.add(createReqDoc("Organization Documents", docs, false));
        reqDocs.add(RequiredDocumentDto.builder()
                .name("Additional Documents")
                .status("Not Uploaded")
                .canUpload(true)
                .build());

        List<VerificationProgressItemDto> progress = new ArrayList<>();
        progress.add(VerificationProgressItemDto.builder().name("Applicant Information").description("Basic details submitted").status("Completed").build());
        progress.add(VerificationProgressItemDto.builder().name("KYC Verification").description("Identity documents pending verification").status("In Progress").build());
        progress.add(VerificationProgressItemDto.builder().name("Organization Verification").description("Company details under review").status("Pending").build());
        progress.add(VerificationProgressItemDto.builder().name("Compliance Review").description("Final compliance checks").status("Pending").build());
        progress.add(VerificationProgressItemDto.builder().name("Approval").description("Access decision pending").status("Pending").build());

        return RequestStatusDto.builder()
                .requestId(req.getRequestId())
                .fullName(req.getFullName())
                .avatarInitials(req.getAvatarInitials())
                .email(req.getEmail())
                .mobileNumber(req.getMobileNumber())
                .companyName(req.getCompanyName())
                .designation(req.getDesignation())
                .industry(req.getIndustry())
                .currentStatus(req.getStatusBadge() != null ? req.getStatusBadge() : "Under Review")
                .nextStep("KYC Verification")
                .nextStepDescription("Please upload and verify all required KYC documents.")
                .submittedOn(req.getSubmittedOn())
                .lastUpdated(req.getLastUpdated())
                .expectedReviewTimeline("2 - 5 Working Days")
                .requestedAccess(req.getRequestedAccess())
                .statusTimeline(req.getTimeline())
                .verificationProgress(progress)
                .requiredDocuments(reqDocs)
                .recentActivities(activityStore.getOrDefault(requestId, new ArrayList<>()))
                .build();
    }

    private RequiredDocumentDto createReqDoc(String name, List<KycDocumentDto> docs, boolean canUpload) {
        Optional<KycDocumentDto> found = docs.stream()
                .filter(d -> d.getDocumentType() != null && d.getDocumentType().equalsIgnoreCase(name) ||
                             d.getCategory() != null && d.getCategory().contains(name.toUpperCase().replace(" ", "_")))
                .findFirst();

        String status = found.map(KycDocumentDto::getStatus).orElse("Under Review");
        String url = found.map(KycDocumentDto::getFileUrl).orElse(null);

        return RequiredDocumentDto.builder()
                .name(name)
                .status(status)
                .viewUrl(url)
                .canUpload(canUpload)
                .build();
    }

    // --- Decision: Approve Request (Image 5 Left) ---
    public ExternalAccessRequestDto approveRequest(String requestId, DecisionRequestDto decision) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        String now = LocalDateTime.now().format(DISPLAY_DT_FMT);
        String startDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        String endDate = LocalDateTime.now().plusYears(1).minusDays(1).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " (1 Year)";

        req.setStatus("APPROVED");
        req.setStatusBadge("Approved");
        req.setApprovedOn(now);
        req.setApprovedBy(decision.getOfficerName() != null ? decision.getOfficerName() + " (" + decision.getDepartment() + ")" : "Compliance Team");
        req.setApprovedAccess(decision.getApprovedAccess() != null && !decision.getApprovedAccess().isEmpty()
                ? decision.getApprovedAccess()
                : req.getRequestedAccess());
        req.setAccessValidFrom(startDate);
        req.setAccessValidTill(endDate);
        req.setWelcomeMessage("Welcome to ATIRATH. You can now access the internal market and trade in ATIRATH Group companies as per the approved access level.");
        req.setLastUpdated(now);

        // Record Activity
        List<ActivityDto> activities = activityStore.computeIfAbsent(requestId, k -> new ArrayList<>());
        activities.add(0, ActivityDto.builder()
                .actorName(decision.getOfficerName() != null ? decision.getOfficerName() : "Compliance Team")
                .actorRole(decision.getDepartment() != null ? decision.getDepartment() : "Compliance")
                .initials("CP")
                .timestamp(now)
                .message("Request approved with valid access until " + endDate)
                .build());

        return req;
    }

    // --- Decision: Reject Request (Image 5 Right) ---
    public ExternalAccessRequestDto rejectRequest(String requestId, DecisionRequestDto decision) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        String now = LocalDateTime.now().format(DISPLAY_DT_FMT);

        req.setStatus("REJECTED");
        req.setStatusBadge("Rejected");
        req.setRejectedOn(now);
        req.setRejectedBy(decision.getOfficerName() != null ? decision.getOfficerName() + " (" + decision.getDepartment() + ")" : "Anitha Verma (Compliance)");
        req.setReasonForRejection(decision.getRejectionReason() != null ? decision.getRejectionReason() :
                "The submitted KYC documents are incomplete. Please provide a valid address proof and updated organization registration certificate.");
        req.setWhatYouCanDo(decision.getWhatYouCanDo() != null && !decision.getWhatYouCanDo().isEmpty() ? decision.getWhatYouCanDo() : Arrays.asList(
                "Review the reason for rejection.",
                "Update the required documents and submit a new request.",
                "For further assistance, contact our support team."
        ));
        req.setLastUpdated(now);

        // Record Activity
        List<ActivityDto> activities = activityStore.computeIfAbsent(requestId, k -> new ArrayList<>());
        activities.add(0, ActivityDto.builder()
                .actorName(decision.getOfficerName() != null ? decision.getOfficerName() : "Anitha Verma")
                .actorRole(decision.getDepartment() != null ? decision.getDepartment() : "Compliance")
                .initials("AV")
                .timestamp(now)
                .message("Request rejected. Reason: " + req.getReasonForRejection())
                .build());

        return req;
    }

    // --- Add Note / Remark ---
    public NoteDto addNote(String requestId, AddNoteRequestDto input) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        String now = LocalDateTime.now().format(DISPLAY_DT_FMT);
        String initials = "ST";
        if (input.getAuthorName() != null && !input.getAuthorName().trim().isEmpty()) {
            String[] parts = input.getAuthorName().trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase();
            }
        }

        NoteDto note = NoteDto.builder()
                .id(noteIdCounter.incrementAndGet())
                .authorName(input.getAuthorName() != null ? input.getAuthorName() : "Compliance Officer")
                .authorRole(input.getAuthorRole() != null ? input.getAuthorRole() : "Compliance")
                .initials(initials)
                .timestamp(now)
                .noteText(input.getNoteText())
                .build();

        if (req.getNotes() == null) {
            req.setNotes(new ArrayList<>());
        }
        req.getNotes().add(0, note);
        req.setLastUpdated(now);

        return note;
    }

    // --- Request More Info ---
    public ActivityDto requestMoreInfo(String requestId, RequestMoreInfoDto input) {
        ExternalAccessRequestDto req = getRequestById(requestId);
        String now = LocalDateTime.now().format(DISPLAY_DT_FMT);

        ActivityDto act = ActivityDto.builder()
                .actorName(input.getRequestedBy() != null ? input.getRequestedBy() : "Compliance Team")
                .actorRole("Compliance")
                .initials("RQ")
                .timestamp(now)
                .message("Additional information requested: " + input.getMessage())
                .build();

        List<ActivityDto> activities = activityStore.computeIfAbsent(requestId, k -> new ArrayList<>());
        activities.add(0, act);
        req.setLastUpdated(now);

        return act;
    }
}
