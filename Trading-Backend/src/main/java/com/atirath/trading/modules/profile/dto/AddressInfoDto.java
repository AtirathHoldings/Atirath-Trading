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
@Schema(description = "Address Information section matching Image 1")
public class AddressInfoDto {

    @Schema(description = "Current Registered / Residential Address", example = "Plot No. 45, Tech Park, Madhapur Hyderabad, Telangana - 500081 India")
    private String currentAddress;
}
