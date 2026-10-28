package com.atirath.trading.modules.auth.dto.common;

import com.atirath.trading.common.ClientType;
import com.atirath.trading.common.UserRole;
import com.atirath.trading.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private UUID id;
    private String userId;
    private String fullName;
    private String email;
    private String countryCode;
    private String phoneNumber;
    private UserRole role;
    private ClientType clientAccessType;
    private UserStatus status;
    private String avatarUrl;
    private boolean twoFactorEnabled;
    private boolean biometricEnabled;
    private List<String> permissions;
    private LocalDateTime lastLoginAt;
}
