package com.atirath.trading.modules.portfolio.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileActionsPageDto {
    private Integer totalCount;
    private String bannerText;
    private String bannerType;   // "INFO", "WARNING"
    private List<MobileActionCardDto> actions;
}
