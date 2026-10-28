package com.atirath.trading.modules.auth.dto.common;

import com.atirath.trading.modules.auth.dto.UserAccountSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresInSeconds;
    private UserAccountSummaryDto user;
}
