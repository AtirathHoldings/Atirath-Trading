package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "General settings matching Image 4")
public class GeneralSettingsDto {

    @Schema(description = "Email Address", example = "rohit.sharma@example.com")
    private String emailAddress;

    @Schema(description = "Email verification status", example = "Verified")
    private String emailStatus;

    @Schema(description = "Mobile Number", example = "+91 98765 43210")
    private String mobileNumber;

    @Schema(description = "Mobile verification status", example = "Verified")
    private String mobileStatus;

    @Schema(description = "Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String company;

    @Schema(description = "Designation", example = "Director")
    private String designation;
}
