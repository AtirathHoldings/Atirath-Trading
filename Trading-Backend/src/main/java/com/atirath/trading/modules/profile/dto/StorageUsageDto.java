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
@Schema(description = "Storage usage statistics matching Image 3")
public class StorageUsageDto {

    @Schema(description = "Used percentage", example = "40")
    private Integer usedPercentage;

    @Schema(description = "Used megabytes label", example = "2.0 MB")
    private String usedMb;

    @Schema(description = "Total capacity label", example = "5.0 MB")
    private String totalMb;

    @Schema(description = "Available megabytes label", example = "3.0 MB")
    private String availableMb;
}
