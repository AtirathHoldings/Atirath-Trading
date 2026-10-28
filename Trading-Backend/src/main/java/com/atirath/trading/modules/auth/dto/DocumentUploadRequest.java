package com.atirath.trading.modules.auth.dto;

import com.atirath.trading.common.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Document type is required")
    @Schema(description = "Type of document: ADDRESS_PROOF, IDENTITY_PROOF, BANK_PASSBOOK, LIVE_PHOTO", example = "IDENTITY_PROOF")
    private DocumentType documentType;

    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name cannot exceed 255 characters")
    @Schema(description = "Original file name", example = "pan_card_front.pdf")
    private String documentName;

    @NotBlank(message = "File path is required")
    @Schema(description = "Secure storage file path or storage URL", example = "documents/user_101/pan_card_front.pdf")
    private String filePath;

    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type cannot exceed 100 characters")
    @Schema(description = "MIME type", example = "application/pdf")
    private String mimeType;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be greater than 0")
    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSizeBytes;
}
