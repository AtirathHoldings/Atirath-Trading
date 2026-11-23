package com.atirath.trading.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    @Schema(description = "Full name of the individual", example = "Sairam V")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email cannot exceed 254 characters")
    @Schema(description = "Email address for login", example = "sairam.v@atirathgroup.com")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must contain 10 to 15 digits")
    @Schema(description = "Mobile number for contact and login", example = "9876543210")
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @Schema(description = "Password for the account", example = "Password@123")
    private String password;

    @Size(max = 50, message = "Referral code cannot exceed 50 characters")
    @Schema(description = "Optional referral code", example = "REF12345", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String referralCode;

    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    @Schema(description = "Optional company or employer name", example = "TechCorp Inc", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String companyName;

    @Size(max = 20, message = "Gender cannot exceed 20 characters")
    @Schema(description = "Optional gender", example = "Male", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String gender;
}
