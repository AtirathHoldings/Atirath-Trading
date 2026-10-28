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
public class KycSubmitRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format (e.g. ABCDE1234F)")
    @Schema(description = "10-character PAN number", example = "ABCDE1234F")
    private String panNumber;

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be a 12-digit number")
    @Schema(description = "12-digit Aadhaar number (will be encrypted at rest)", example = "123456789012")
    private String aadhaarNumber;

    @NotBlank(message = "Name as per PAN is required")
    @Size(max = 150, message = "Name cannot exceed 150 characters")
    @Schema(description = "Full name as printed on PAN card", example = "Sairam V")
    private String panFullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(description = "Date of birth (YYYY-MM-DD)", example = "1995-05-15")
    private LocalDate dateOfBirth;
}
