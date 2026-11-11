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
@Schema(description = "Language & Region settings matching Image 4")
public class LanguageRegionDto {

    @Schema(description = "Selected Language", example = "English (India)")
    private String language;

    @Schema(description = "Selected Timezone", example = "(UTC+05:30) Asia/Kolkata")
    private String timezone;
}
