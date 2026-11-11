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
@Schema(description = "Note and remark entry for an external access request")
public class NoteDto {

    @Schema(description = "Note ID", example = "1")
    private Long id;

    @Schema(description = "Staff member name", example = "Anitha Verma")
    private String authorName;

    @Schema(description = "Staff department/role", example = "Compliance")
    private String authorRole;

    @Schema(description = "Staff avatar initials", example = "AV")
    private String initials;

    @Schema(description = "Timestamp when note was recorded", example = "16 Sep 2026, 09:30 AM")
    private String timestamp;

    @Schema(description = "Note text content", example = "Initial verification completed. KYC documents pending.")
    private String noteText;
}
