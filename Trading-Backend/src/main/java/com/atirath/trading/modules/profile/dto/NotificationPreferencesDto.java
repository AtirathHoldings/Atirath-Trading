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
@Schema(description = "Notification preferences toggles matching Image 2")
public class NotificationPreferencesDto {

    @Schema(description = "Trade Notifications (Order updates, confirmations)", example = "true")
    private Boolean tradeNotifications;

    @Schema(description = "Negotiation Messages (New messages, mentions)", example = "true")
    private Boolean negotiationMessages;

    @Schema(description = "Settlement Updates (Payouts, refunds)", example = "true")
    private Boolean settlementUpdates;

    @Schema(description = "KYC & Compliance (Verification updates)", example = "true")
    private Boolean kycCompliance;

    @Schema(description = "Member Requests (New member requests, status)", example = "true")
    private Boolean memberRequests;

    @Schema(description = "System Updates (Platform announcements)", example = "true")
    private Boolean systemUpdates;

    @Schema(description = "Security Alerts (Login alerts, password changes)", example = "true")
    private Boolean securityAlerts;

    @Schema(description = "Marketing Communications (Product updates, offers)", example = "false")
    private Boolean marketingCommunications;
}
