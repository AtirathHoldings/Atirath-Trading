package com.atirath.trading.modules.admin.system.dto;

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
public class NotificationConfigPageDto {
    private AdminUserHeaderDto adminUser;
    private String pageTitle;
    private String pageSubtitle;
    private String selectedCompany;

    // 4 KPIs
    private List<KpiCardDto> kpis;

    // Tabs
    private List<String> tabs;
    private String activeTab;

    // Filter Options
    private List<String> categories;
    private List<String> channels;
    private List<String> statuses;

    // Table rows
    private List<NotificationItemDto> notifications;
    private Integer totalNotifications;
    private Integer currentPage;
    private Integer totalPages;
    private Integer pageSize;

    // Bottom Visualizations
    private NotificationsByCategoryDonutDto notificationsByCategory;
    private List<NotificationChannelBarDto> notificationsByChannel;
    private List<NotificationTrendMonthDto> notificationTrend;

    // Preselected Drawer Detail
    private NotificationDetailDto selectedNotificationDetail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItemDto {
        private String id;
        private Integer number;
        private String notificationName;
        private String eventTrigger;
        private String category;
        private String categoryTheme; // blue, orange, purple
        private String channels;
        private String channelsTheme; // blue, green, teal
        private String status;
        private String statusTheme; // green, red
        private String lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationsByCategoryDonutDto {
        private String totalNotifications;
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
    public static class NotificationChannelBarDto {
        private String channel;
        private Double percentage;
        private Integer count;
        private String color;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationTrendMonthDto {
        private String month;
        private Integer created;
        private Integer sent;
    }
}
