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
@Schema(description = "Additional Information section matching Image 1")
public class AdditionalInfoDto {

    @Schema(description = "Languages Known")
    @Builder.Default
    private List<String> languageKnown = new ArrayList<>();

    @Schema(description = "How did you hear about us", example = "Referral")
    private String howDidYouHearAboutUs;

    @Schema(description = "Profile Visibility setting", example = "Visible to verified members")
    private String profileVisibility;
}
