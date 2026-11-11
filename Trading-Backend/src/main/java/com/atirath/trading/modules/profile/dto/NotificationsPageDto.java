package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notifications page payload matching Image 2")
public class NotificationsPageDto {

    // --- Header ---
    @Schema(description = "Member ID", example = "ATG10027")
    private String memberId;

    @Schema(description = "Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Member Status Badge", example = "Verified Member")
    private String memberBadge;

    @Schema(description = "Email", example = "rohit.sharma@example.com")
    private String email;

    @Schema(description = "Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Designation", example = "Director")
    private String designation;

    @Schema(description = "Industry", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Member Since", example = "12 Jan 2021")
    private String memberSince;

    @Schema(description = "Avatar display initials", example = "RS")
    private String avatarInitials;

    // --- Notifications List & Metrics ---
    @Schema(description = "Total unread notifications count", example = "2")
    private Integer unreadCount;

    @Schema(description = "List of notifications")
    @Builder.Default
    private List<NotificationItemDto> notifications = new ArrayList<>();

    // --- Preferences & Channels ---
    @Schema(description = "Notification preferences by category")
    private NotificationPreferencesDto preferences;

    @Schema(description = "Notification delivery channels")
    private NotificationChannelsDto channels;
}
