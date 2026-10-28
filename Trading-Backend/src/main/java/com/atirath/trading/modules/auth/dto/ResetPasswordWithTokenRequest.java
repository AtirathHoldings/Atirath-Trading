package com.atirath.trading.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordWithTokenRequest {

    @NotBlank(message = "Reset token is required")
    @Schema(description = "Password reset token received via email", example = "d4e5f6a7-1234-5678-90ab-cdef12345678")
    private String token;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @Schema(description = "New password", example = "NewPassword@123")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Confirm new password", example = "NewPassword@123")
    private String confirmPassword;
}
