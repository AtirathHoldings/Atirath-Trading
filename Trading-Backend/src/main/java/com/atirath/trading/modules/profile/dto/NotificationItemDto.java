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
@Schema(description = "Notification entry on Notifications screen matching Image 2")
public class NotificationItemDto {

    @Schema(description = "Notification ID", example = "notif-1")
    private String id;

    @Schema(description = "Notification Title", example = "Trade Order Approved")
    private String title;

    @Schema(description = "Notification Message", example = "Your buy order #TRD00234 for Solar Panels has been approved.")
    private String message;

    @Schema(description = "Type category: TRADE, NEGOTIATION, SETTLEMENT, KYC, MEMBER_REQUEST, SYSTEM, REMINDER, SECURITY", example = "TRADE")
    private String type;

    @Schema(description = "Relative time label", example = "10 mins ago")
    private String timeAgo;

    @Schema(description = "Whether the notification is unread", example = "true")
    private Boolean isUnread;

    @Schema(description = "Associated icon color or theme", example = "green")
    private String iconTheme;
}
