package com.atirath.trading.modules.auth.dto.mobile;

import com.atirath.trading.common.AccountStatus;
import com.atirath.trading.common.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Compact Mobile Profile (Fast loading & lightweight)")
public class MobileProfileDto {

    private Long userId;
    private AccountType accountType;
    private String displayName;
    private String email;
    private String mobileNumber;
    private AccountStatus status;
    private String referralCode;

    // Compact KYC status
    private boolean kycCompleted;
    private String panNumber;
    private String maskedAadhaarNumber;

    // Compact Bank status
    private boolean bankAdded;
    private String bankName;
    private String maskedAccountNumber;
}
