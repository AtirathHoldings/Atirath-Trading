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
@Schema(description = "Login history log record matching Image 4")
public class LoginHistoryItemDto {

    @Schema(description = "Date & Time", example = "16 Sep 2026, 10:24 AM")
    private String dateTime;

    @Schema(description = "Device", example = "Windows · Chrome")
    private String device;

    @Schema(description = "Location", example = "Hyderabad, India")
    private String location;

    @Schema(description = "IP Address", example = "103.21.45.67")
    private String ipAddress;

    @Schema(description = "Login Status: Successful, Unsuccessful", example = "Successful")
    private String status;
}
