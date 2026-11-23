package com.atirath.trading.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorporateRegisterRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name cannot exceed 200 characters")
    @Schema(description = "Company or institution name", example = "Atirath Technologies Pvt Ltd")
    private String companyName;

    @NotBlank(message = "Registration ID / CIN is required")
    @Size(max = 30, message = "Registration ID cannot exceed 30 characters")
    @Schema(description = "Company Registered ID or CIN number", example = "U72200TG2020PTC123456")
    private String registrationId;

    @NotBlank(message = "Contact person name is required")
    @Size(max = 150, message = "Contact person name cannot exceed 150 characters")
    @Schema(description = "Name of the authorized representative/contact person", example = "Rajesh Sharma")
    private String contactPersonName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email cannot exceed 254 characters")
    @Schema(description = "Corporate official email for login", example = "corporate@atirathgroup.com")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must contain 10 to 15 digits")
    @Schema(description = "Corporate contact mobile number", example = "9811122233")
    private String mobileNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @Schema(description = "Account login password", example = "Corporate@123")
    private String password;

    @Size(max = 50, message = "Referral code cannot exceed 50 characters")
    @Schema(description = "Optional referral code", example = "CORP_REF", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String referralCode;

    @Size(max = 150, message = "Industry cannot exceed 150 characters")
    @Schema(description = "Optional industry domain", example = "Information Technology", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String industry;

    @Schema(description = "Optional date of incorporation (YYYY-MM-DD)", example = "2020-01-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate incorporationDate;

    @Size(max = 50, message = "Company type cannot exceed 50 characters")
    @Schema(description = "Optional company type", example = "Private Limited", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String companyType;
}
