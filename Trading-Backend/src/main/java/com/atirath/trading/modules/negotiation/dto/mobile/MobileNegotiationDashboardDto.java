package com.atirath.trading.modules.negotiation.dto.mobile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mobile Screen 1: Negotiation Dashboard
 * Delivers lightweight KPI counters, active filter tab, and cards list.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNegotiationDashboardDto {
    private MobileNegotiationCountersDto counters;
    private String selectedTab;
    private List<MobileNegotiationCardDto> negotiations;
}
