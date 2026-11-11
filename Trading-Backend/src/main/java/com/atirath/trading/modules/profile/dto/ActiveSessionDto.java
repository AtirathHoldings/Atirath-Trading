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
@Schema(description = "Active session device item matching Image 4")
public class ActiveSessionDto {

    @Schema(description = "Session ID", example = "sess-win-01")
    private String sessionId;

    @Schema(description = "Device Name", example = "Windows · Chrome")
    private String device;

    @Schema(description = "Location", example = "Hyderabad, India")
    private String location;

    @Schema(description = "IP Address", example = "103.21.45.67")
    private String ipAddress;

    @Schema(description = "Whether this is the current active session", example = "true")
    private Boolean isCurrentSession;
}
