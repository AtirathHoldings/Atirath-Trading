package com.atirath.trading.modules.profile.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Mobile KYC submission payload to update/submit user KYC details into database.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileKycSubmitRequestDto implements Serializable {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format. Must be 5 letters, 4 digits, 1 letter.")
    private String panNumber;

    @NotBlank(message = "Name on PAN is required")
    private String panFullName;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be exactly 12 digits.")
    private String aadhaarNumber;

    private String dateOfBirth; // YYYY-MM-DD

    private String address;

    // Optional bank link during KYC
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String branchName;
}
