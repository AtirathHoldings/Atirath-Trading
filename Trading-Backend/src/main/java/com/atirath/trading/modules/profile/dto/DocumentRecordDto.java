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
@Schema(description = "Document row in the Documents table matching Image 3")
public class DocumentRecordDto {

    @Schema(description = "Document ID", example = "doc-1")
    private String id;

    @Schema(description = "Document Name", example = "PAN Card")
    private String name;

    @Schema(description = "Filename", example = "pan_card.pdf")
    private String fileName;

    @Schema(description = "Category: Identity, Organization, Bank, Other", example = "Identity")
    private String category;

    @Schema(description = "Upload Date", example = "12 Jan 2024")
    private String uploadedOn;

    @Schema(description = "Expiry Date (if applicable)", example = "-")
    private String expiryDate;

    @Schema(description = "Verification Status: Verified, Under Review, Expired", example = "Verified")
    private String status;

    @Schema(description = "File URL")
    private String fileUrl;
}
