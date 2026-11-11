package com.atirath.trading.modules.externalrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for adding a note or remark to an external request")
public class AddNoteRequestDto {

    @NotBlank(message = "Note text cannot be empty")
    @Schema(description = "Note text content", example = "Initial verification completed. KYC documents pending.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String noteText;

    @Schema(description = "Author name", example = "Anitha Verma")
    private String authorName;

    @Schema(description = "Author department", example = "Compliance")
    private String authorRole;
}
