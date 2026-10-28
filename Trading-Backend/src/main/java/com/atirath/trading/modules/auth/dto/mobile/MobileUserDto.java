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
@Schema(description = "Lightweight User Payload optimized for Mobile App")
public class MobileUserDto {

    private Long userId;
    private AccountType accountType;
    private String displayName; // Full Name or Company Name
    private String mobileNumber;
    private String email;
    private AccountStatus status;
    private boolean kycCompleted;
    private boolean bankAdded;
}
