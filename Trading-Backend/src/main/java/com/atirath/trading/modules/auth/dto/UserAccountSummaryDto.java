package com.atirath.trading.modules.auth.dto;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountSummaryDto {

    private Long userId;
    private AccountType accountType;
    private String email;
    private String mobileNumber;
    private String referralCode;
    private AccountStatus accountStatus;
    private String displayName; // Full Name or Company Name
    private OffsetDateTime createdAt;
}
