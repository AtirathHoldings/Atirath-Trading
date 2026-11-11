package com.atirath.trading.modules.externalrequest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "KYC Document details and verification status")
public class KycDocumentDto {

    @Schema(description = "Document ID", example = "101")
    private Long id;

    @Schema(description = "Category", example = "IDENTITY_DOCUMENT")
    private String category;

    @Schema(description = "Document Type", example = "PAN Card")
    private String documentType;

    @Schema(description = "Document Number / Identifier", example = "ABCDE1234F")
    private String documentNumber;

    @Schema(description = "Name on Document", example = "Rohit Sharma")
    private String holderName;

    @Schema(description = "Document Verification Status (Verified, Under Review, Not Verified, Not Uploaded)", example = "Verified")
    private String status;

    @Schema(description = "Issue Date", example = "01 Sep 2026")
    private String issueDate;

    @Schema(description = "Submitted On timestamp", example = "15 Sep 2026, 10:30 AM")
    private String submittedOn;

    @Schema(description = "Verified On timestamp", example = "15 Sep 2026, 02:15 PM")
    private String verifiedOn;

    @Schema(description = "Verified by staff member", example = "Anitha Verma (Compliance)")
    private String verifiedBy;

    @Schema(description = "Compliance remarks or notes", example = "Document clear. Waiting for UIDAI verification.")
    private String remarks;

    @Schema(description = "Document file / image URL")
    private String fileUrl;

    @Schema(description = "Registration Number (for corporate documents)", example = "U40100TG2020PTC123456")
    private String registrationNumber;

    @Schema(description = "Company Name (for corporate documents)", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Address on document (for address proofs)", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
    private String address;
}
