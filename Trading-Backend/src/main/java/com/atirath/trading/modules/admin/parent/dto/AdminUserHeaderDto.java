package com.atirath.trading.modules.admin.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserHeaderDto {
    private String name;
    private String initials;
    private String role;
    private String currentDate;
    private int unreadNotifications;
}
