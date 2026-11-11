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
@Schema(description = "Device item in Active Devices list matching Image 1")
public class DeviceItemDto {

    @Schema(description = "Device ID", example = "dev-1")
    private String id;

    @Schema(description = "Device title", example = "Windows - Chrome")
    private String name;

    @Schema(description = "Device category icon type: LAPTOP, MOBILE, DESKTOP", example = "LAPTOP")
    private String deviceType;

    @Schema(description = "OS & Browser detail", example = "Windows 11 | Chrome 128.0.6613.137")
    private String osBrowser;

    @Schema(description = "Geo location", example = "Hyderabad, India")
    private String location;

    @Schema(description = "IP address", example = "103.21.45.67")
    private String ipAddress;

    @Schema(description = "Status badge: Active Now, Active, Inactive", example = "Active Now")
    private String status;

    @Schema(description = "Whether this device is the current active session", example = "true")
    private Boolean isCurrentDevice;

    @Schema(description = "Last active timestamp", example = "15 Sep 2026, 06:24 PM")
    private String lastActive;

    @Schema(description = "Whether access can be revoked", example = "false")
    private Boolean canRevoke;
}
