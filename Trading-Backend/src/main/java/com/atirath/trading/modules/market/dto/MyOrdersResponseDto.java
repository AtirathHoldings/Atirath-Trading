package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyOrdersResponseDto {

    @Schema(description = "Summary counts across all status tabs")
    private OrderTabCountsDto summaryCounts;

    @Schema(description = "List of orders on current page")
    private List<MyOrderRowDto> orders;

    @Schema(description = "Pagination metadata")
    private PaginationMetadata pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderTabCountsDto {
        private Long allCount;        // e.g. 28
        private Long openCount;       // e.g. 3
        private Long inProgressCount; // e.g. 2
        private Long completedCount;  // e.g. 12
        private Long cancelledCount;  // e.g. 1
        private Long rejectedCount;   // e.g. 1
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyOrderRowDto {
        private Long orderId;
        private String orderNo;             // "BUY25091814322"
        private String companyName;         // "Atirath Holdings India Limited"
        private String symbol;              // "ATG-H"
        private String logoUrl;
        private String type;                // "BUY" or "SELL"
        private String typeBadge;           // "Buy" or "Sell"
        private Long quantity;              // 1000
        private BigDecimal pricePerShare;   // 242.00
        private BigDecimal orderValue;      // 242000.00
        private String status;              // "IN_ESCROW", "PENDING_MATCH", "COMPLETED", "CANCELLED", "REJECTED"
        private String statusBadge;         // "In Escrow", "Pending Match", "Completed", "Cancelled", "Rejected"
        private String statusColor;         // "blue", "yellow", "green", "grey", "red"
        private String orderDate;           // "18 Sep 2025 02:32 PM"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationMetadata {
        private Integer currentPage;    // 1
        private Integer pageSize;       // 10
        private Long totalElements;     // 28
        private Integer totalPages;     // 3
        private String showingText;     // "Showing 1 to 10 of 28 orders"
    }
}
