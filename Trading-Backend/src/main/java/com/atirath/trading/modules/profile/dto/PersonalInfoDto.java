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
@Schema(description = "Personal Information section matching Image 1")
public class PersonalInfoDto {

    @Schema(description = "Full Name", example = "Rohit Sharma")
    private String fullName;

    @Schema(description = "Date of Birth", example = "12 Mar 1995")
    private String dateOfBirth;

    @Schema(description = "Gender", example = "Male")
    private String gender;

    @Schema(description = "Nationality", example = "Indian")
    private String nationality;

    @Schema(description = "PAN Number", example = "ABCDE1234F")
    private String panNumber;

    @Schema(description = "Aadhaar Number", example = "1234 5678 9012")
    private String aadhaarNumber;

    @Schema(description = "Aadhaar verification status", example = "Verified")
    private String aadhaarStatus;

    @Schema(description = "Occupation", example = "Business Professional")
    private String occupation;

    @Schema(description = "LinkedIn Profile URL", example = "https://linkedin.com/in/rohitsharma")
    private String linkedInProfile;
}
