package com.atirath.trading.modules.admin.subsidiary.operations.dto;

import com.atirath.trading.modules.admin.parent.dto.AdminUserHeaderDto;
import com.atirath.trading.modules.admin.parent.dto.KpiCardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketsPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;
    private String dateRange;

    // 5 KPIs + Avg Resolution Time
    private List<KpiCardDto> kpis;
    private String avgResolutionTime;
    private String avgResolutionTimeChange;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> companies;
    private List<String> categories;
    private List<String> priorities;
    private List<String> statuses;

    // Table rows
    private List<TicketItemDto> tickets;
    private Integer totalTickets;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private TicketsCategoryDonutDto ticketsByCategory;
    private List<TicketStatusBarDto> ticketsByStatus;
    private List<TicketTrendMonthDto> ticketsTrend;

    // Preselected Drawer Detail
    private SupportTicketDetailDto selectedTicketDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketItemDto {
        private String id;
        private Integer number;
        private String ticketId;
        private String subject;
        private String category;
        private String categoryTheme; // blue, purple, teal, orange, red, gray
        private String company;
        private String raisedBy;
        private String priority;
        private String priorityTheme; // red, orange, green
        private String status;
        private String statusTheme; // red, blue, green, gray
        private String raisedOn;
        private String expectedResolution;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketsCategoryDonutDto {
        private String totalTickets;
        private List<CategorySliceDto> slices;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySliceDto {
        private String category;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketStatusBarDto {
        private String status;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TicketTrendMonthDto {
        private String month;
        private Integer open;
        private Integer inProgress;
        private Integer resolved;
        private Integer closed;
    }
}
