package com.atirath.trading.modules.profile.dto;

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
@Schema(description = "KYC Document Item on Member KYC page matching Image 2")
public class KycDocumentItemDto {

    @Schema(description = "Document ID", example = "1")
    private Long id;

    @Schema(description = "Document Section Title", example = "Identity Proof (PAN)")
    private String title;

    @Schema(description = "Description instructions", example = "Upload your PAN card for identity verification.")
    private String description;

    @Schema(description = "Document Type label", example = "PAN Card")
    private String documentType;

    @Schema(description = "Document Number / Identifier", example = "ABCDE1234F")
    private String documentNumber;

    @Schema(description = "Upload date formatted", example = "15 Sep 2026")
    private String uploadedOn;

    @Schema(description = "Verification Status: Verified, Under Review, Not Verified", example = "Verified")
    private String status;

    @Schema(description = "Document preview / file URL")
    private String fileUrl;

    @Schema(description = "Whether upload button should be shown", example = "false")
    private Boolean canUpload;
}
