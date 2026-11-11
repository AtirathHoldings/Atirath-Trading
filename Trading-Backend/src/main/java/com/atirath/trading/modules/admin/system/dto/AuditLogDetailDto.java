package com.atirath.trading.modules.admin.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDetailDto {
    private String id;
    private String status;
    private String statusTheme;
    private String timestamp;
    private String initials;
    private String userName;
    private String role;
    private String email;

    // Action Information
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private String device;
    private String location;

    // Additional Details
    private String sessionId;
    private String userAgent;
    private String loginMethod;
    private String mfaUsed;
    private String duration;

    // Related Logs
    private List<RelatedLogDto> relatedLogs;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RelatedLogDto {
        private String eventText;
        private String timeText;
        private String dotColor;
    }
}
