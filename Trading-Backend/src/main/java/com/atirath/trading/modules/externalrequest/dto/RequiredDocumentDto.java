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
@Schema(description = "Required Document checklist item on Request Status screen")
public class RequiredDocumentDto {

    @Schema(description = "Document name", example = "PAN Card")
    private String name;

    @Schema(description = "Upload/verification state: Uploaded, Under Review, Not Uploaded, Verified", example = "Uploaded")
    private String status;

    @Schema(description = "Download/View URL if uploaded")
    private String viewUrl;

    @Schema(description = "Whether document upload action is available", example = "false")
    private Boolean canUpload;
}
