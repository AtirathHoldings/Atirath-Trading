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
@Schema(description = "Document expiry alert matching Image 3")
public class ExpiryAlertDto {

    @Schema(description = "Document Name", example = "Trading Authorization")
    private String name;

    @Schema(description = "Alert text / Expiry duration", example = "Expires in 30 days")
    private String alertText;

    @Schema(description = "Expiry Date", example = "15 Apr 2025")
    private String expiryDate;

    @Schema(description = "Severity theme: red, green, orange", example = "red")
    private String theme;
}
