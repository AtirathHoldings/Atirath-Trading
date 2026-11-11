package com.atirath.trading.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Professional Details section matching Image 1")
public class ProfessionalDetailsDto {

    @Schema(description = "Total Years of Professional Experience", example = "8+ Years")
    private String yearsOfExperience;

    @Schema(description = "Area of Professional Expertise", example = "Renewable Energy, Project Development")
    private String areaOfExpertise;

    @Schema(description = "Interested Sectors tags")
    @Builder.Default
    private List<String> interestedSectors = new ArrayList<>();

    @Schema(description = "Target Investment Range", example = "₹10,00,000 - ₹50,00,000")
    private String investmentRange;

    @Schema(description = "Primary Investment Purpose", example = "Strategic Investment")
    private String investmentPurpose;
}
