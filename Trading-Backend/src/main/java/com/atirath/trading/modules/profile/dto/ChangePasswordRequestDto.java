package com.atirath.trading.modules.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for changing account password")
public class ChangePasswordRequestDto {

    @NotBlank(message = "Current password is required")
    @Schema(description = "Current password", example = "OldPass@123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Schema(description = "New password", example = "NewSecurePass@2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    @Schema(description = "Confirm new password", example = "NewSecurePass@2026", requiredMode = Schema.RequiredMode.REQUIRED)
    private String confirmPassword;
}
