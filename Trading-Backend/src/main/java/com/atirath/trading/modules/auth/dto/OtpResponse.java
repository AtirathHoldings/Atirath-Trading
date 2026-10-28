package com.atirath.trading.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponse {

    private boolean success;
    private String message;
    private long expiresInSeconds;
    private String sessionToken;
    private String devOtp; // Populated in dev mode for easy testing
}
