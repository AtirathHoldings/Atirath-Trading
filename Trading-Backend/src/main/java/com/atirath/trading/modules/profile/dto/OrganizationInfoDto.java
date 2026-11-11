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
@Schema(description = "Organization Information section matching Image 1")
public class OrganizationInfoDto {

    @Schema(description = "Organization / Company Name", example = "GreenTech Solutions Pvt Ltd")
    private String companyName;

    @Schema(description = "Industry category", example = "Renewable Energy")
    private String industry;

    @Schema(description = "Company Legal Entity Type", example = "Private Limited")
    private String companyType;

    @Schema(description = "Designation of Member", example = "Director")
    private String designation;

    @Schema(description = "Company PAN", example = "AABCG1234H")
    private String companyPan;

    @Schema(description = "Registration / CIN Number", example = "U40100TG2020PTC123456")
    private String registrationNumber;

    @Schema(description = "Company Registered Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081")
    private String companyAddress;

    @Schema(description = "Company Website URL", example = "www.greentechsolutions.com")
    private String website;

    @Schema(description = "Brand Logo or Tagline", example = "GreenTech Solutions Renewable Energy for a Better Tomorrow")
    private String logoUrl;
}
