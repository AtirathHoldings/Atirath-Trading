package com.atirath.trading.modules.market.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Lightweight DTO specifically for Flutter my_orders_screen.dart.
 * Contains status tab counter badges and concise order card models.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileMyOrdersResponseDto {
    private Map<String, Long> tabCounts; // {"ALL": 28, "OPEN": 3, "IN_PROGRESS": 2, "COMPLETED": 12, "CANCELLED": 1, "REJECTED": 1}
    private List<MobileOrderCardDto> orders;
    private Integer totalOrders;
    private Integer currentPage;
    private Integer totalPages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MobileOrderCardDto {
        private Long orderId;
        private String orderNo;            // e.g. "BUY25091814322"
        private String symbol;             // "ATG-H"
        private String companyName;        // "Atirath Holdings India Limited"
        private String side;               // "BUY" or "SELL"
        private String orderType;          // "LIMIT" or "MARKET"
        private Long quantity;             // 1000
        private BigDecimal price;          // 242.00
        private BigDecimal orderValue;     // 242000.00
        private String status;             // "IN_ESCROW", "PENDING_MATCH", "COMPLETED", "CANCELLED", "REJECTED"
        private String statusBadge;        // "In Escrow", "Pending Match", "Completed", "Cancelled", "Rejected"
        private String statusColor;        // "blue", "yellow", "green", "grey", "red"
        private String orderDateFormatted; // "18 Sep 2025, 02:32 PM"
        private Boolean canCancel;
        private Boolean canModify;
    }
}
