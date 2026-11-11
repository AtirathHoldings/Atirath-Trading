package com.atirath.trading.modules.externalrequest.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.externalrequest.dto.*;
import com.atirath.trading.modules.externalrequest.service.ExternalRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/external-requests")
@RequiredArgsConstructor
@Tag(name = "External Member Requests", description = "Endpoints for External Access Requests, Applicant Profile, KYC Review, Status Tracking, and Approval/Rejection")
public class ExternalRequestController {

    private final ExternalRequestService externalRequestService;

    @GetMapping
    @Operation(summary = "List External Member Requests", description = "Retrieves all external member access requests with optional status and search filtering")
    public ResponseEntity<ApiResponse<List<ExternalRequestListItemDto>>> getAllRequests(
            @Parameter(description = "Filter by status (e.g. ALL, UNDER_REVIEW, APPROVED, REJECTED, SUBMITTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Search term by applicant name, company, email, or request ID")
            @RequestParam(required = false) String search
    ) {
        List<ExternalRequestListItemDto> list = externalRequestService.getAllRequests(search, status);
        return ResponseEntity.ok(ApiResponse.success(list, "External member requests retrieved successfully"));
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get Applicant Profile Overview", description = "Retrieves the full applicant profile, personal & organization info, requested access, notes, and timeline (Image 2)")
    public ResponseEntity<ApiResponse<ExternalAccessRequestDto>> getRequestById(
            @PathVariable String requestId
    ) {
        ExternalAccessRequestDto detail = externalRequestService.getRequestById(requestId);
        return ResponseEntity.ok(ApiResponse.success(detail, "Applicant profile retrieved successfully"));
    }

    @PostMapping
    @Operation(summary = "Submit External Access Request", description = "Submits a new membership access request with 5-step applicant and organization details (Image 1)")
    public ResponseEntity<ApiResponse<ExternalAccessRequestDto>> submitRequest(
            @Valid @RequestBody SubmitExternalAccessRequestDto input
    ) {
        ExternalAccessRequestDto created = externalRequestService.submitRequest(input, false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "External access request submitted successfully with ID: " + created.getRequestId()));
    }

    @PostMapping("/draft")
    @Operation(summary = "Save Request as Draft", description = "Saves partially filled external member access request without enforcing strict validations (Image 1 'Save as Draft')")
    public ResponseEntity<ApiResponse<ExternalAccessRequestDto>> saveDraft(
            @RequestBody SubmitExternalAccessRequestDto input
    ) {
        ExternalAccessRequestDto draft = externalRequestService.submitRequest(input, true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(draft, "External access request saved as draft with ID: " + draft.getRequestId()));
    }

    @GetMapping("/{requestId}/kyc")
    @Operation(summary = "Get KYC Review Details", description = "Retrieves identity, address, organization documents, compliance checklist, and verification history (Image 3)")
    public ResponseEntity<ApiResponse<KycReviewDto>> getKycReview(
            @PathVariable String requestId
    ) {
        KycReviewDto kyc = externalRequestService.getKycReview(requestId);
        return ResponseEntity.ok(ApiResponse.success(kyc, "KYC review details retrieved successfully"));
    }

    @PutMapping("/{requestId}/kyc/documents/{docId}")
    @Operation(summary = "Update KYC Document Status", description = "Marks an individual document as Verified, Under Review, or Not Verified with remarks and reviewer info")
    public ResponseEntity<ApiResponse<KycDocumentDto>> updateKycDocumentStatus(
            @PathVariable String requestId,
            @PathVariable Long docId,
            @Valid @RequestBody UpdateDocumentStatusDto update
    ) {
        KycDocumentDto updatedDoc = externalRequestService.updateKycDocumentStatus(requestId, docId, update);
        return ResponseEntity.ok(ApiResponse.success(updatedDoc, "Document verification status updated to " + update.getStatus()));
    }

    @GetMapping("/{requestId}/status")
    @Operation(summary = "Get Request Status & Progress", description = "Retrieves current status, milestone timeline, verification progress, and required documents status (Image 4)")
    public ResponseEntity<ApiResponse<RequestStatusDto>> getRequestStatus(
            @PathVariable String requestId
    ) {
        RequestStatusDto status = externalRequestService.getRequestStatus(requestId);
        return ResponseEntity.ok(ApiResponse.success(status, "Request tracking status retrieved successfully"));
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve External Access Request", description = "Approves external access request, grants permissions, sets validity period, and generates welcome message (Image 5 Left)")
    public ResponseEntity<ApiResponse<ExternalAccessRequestDto>> approveRequest(
            @PathVariable String requestId,
            @RequestBody(required = false) DecisionRequestDto decision
    ) {
        if (decision == null) {
            decision = DecisionRequestDto.builder().decision("APPROVE").officerName("Compliance Team").department("Compliance").build();
        }
        ExternalAccessRequestDto approved = externalRequestService.approveRequest(requestId, decision);
        return ResponseEntity.ok(ApiResponse.success(approved, "External member request " + requestId + " has been approved successfully"));
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject External Access Request", description = "Rejects external access request with reason and actionable remediation steps for applicant (Image 5 Right)")
    public ResponseEntity<ApiResponse<ExternalAccessRequestDto>> rejectRequest(
            @PathVariable String requestId,
            @RequestBody(required = false) DecisionRequestDto decision
    ) {
        if (decision == null) {
            decision = DecisionRequestDto.builder()
                    .decision("REJECT")
                    .officerName("Anitha Verma")
                    .department("Compliance")
                    .rejectionReason("The submitted KYC documents are incomplete. Please provide a valid address proof and updated organization registration certificate.")
                    .build();
        }
        ExternalAccessRequestDto rejected = externalRequestService.rejectRequest(requestId, decision);
        return ResponseEntity.ok(ApiResponse.success(rejected, "External member request " + requestId + " has been rejected"));
    }

    @PostMapping("/{requestId}/notes")
    @Operation(summary = "Add Compliance/Operations Note", description = "Adds a timestamped note and remark to the applicant's record (Image 2 Quick Actions)")
    public ResponseEntity<ApiResponse<NoteDto>> addNote(
            @PathVariable String requestId,
            @Valid @RequestBody AddNoteRequestDto input
    ) {
        NoteDto note = externalRequestService.addNote(requestId, input);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(note, "Note added successfully"));
    }

    @GetMapping("/{requestId}/notes")
    @Operation(summary = "List Notes for Request", description = "Retrieves all notes and remarks recorded for an applicant")
    public ResponseEntity<ApiResponse<List<NoteDto>>> getNotes(
            @PathVariable String requestId
    ) {
        ExternalAccessRequestDto req = externalRequestService.getRequestById(requestId);
        return ResponseEntity.ok(ApiResponse.success(req.getNotes(), "Notes retrieved successfully"));
    }

    @PostMapping("/{requestId}/request-info")
    @Operation(summary = "Request More Information", description = "Sends an inquiry or request for additional documents/clarification to the applicant")
    public ResponseEntity<ApiResponse<ActivityDto>> requestMoreInfo(
            @PathVariable String requestId,
            @Valid @RequestBody RequestMoreInfoDto input
    ) {
        ActivityDto activity = externalRequestService.requestMoreInfo(requestId, input);
        return ResponseEntity.ok(ApiResponse.success(activity, "Information request sent to applicant"));
    }
}
