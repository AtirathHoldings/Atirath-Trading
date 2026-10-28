package com.atirath.trading.modules.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreviewResponse {

    @Schema(description = "Company ID")
    private Long companyId;

    @Schema(description = "Company Name", example = "Atirath Holdings India Limited")
    private String companyName;

    @Schema(description = "Symbol", example = "ATG-H")
    private String symbol;

    @Schema(description = "Sector", example = "Diversified")
    private String sector;

    @Schema(description = "Current market price", example = "245.00")
    private BigDecimal currentPrice;

    @Schema(description = "Best Ask price available in order book", example = "246.00")
    private BigDecimal bestAsk;

    @Schema(description = "Best Bid price available in order book", example = "244.00")
    private BigDecimal bestBid;

    @Schema(description = "Order side (BUY or SELL)", example = "BUY")
    private String orderSide;

    @Schema(description = "Order type (LIMIT, MARKET, STOP_LOSS)", example = "LIMIT")
    private String orderType;

    @Schema(description = "Quantity of shares", example = "1000")
    private Long quantity;

    @Schema(description = "Price per share", example = "242.00")
    private BigDecimal pricePerShare;

    @Schema(description = "Raw order value (quantity * price)", example = "242000.00")
    private BigDecimal orderValue;

    @Schema(description = "Order validity", example = "Day Order (Valid till end of trading day)")
    private String validity;

    @Schema(description = "Stop-loss enabled flag")
    private Boolean stopLossEnabled;

    @Schema(description = "Stop price if enabled")
    private BigDecimal stopPrice;

    @Schema(description = "Trigger type")
    private String triggerType;

    // --- Estimated Charges Breakdown ---
    @Schema(description = "Transaction Fee percentage (0.1%)", example = "0.001")
    private BigDecimal transactionFeeRate;

    @Schema(description = "Transaction Fee amount (0.1% of order value)", example = "242.00")
    private BigDecimal transactionFee;

    @Schema(description = "GST percentage on fee (18%)", example = "0.18")
    private BigDecimal gstRate;

    @Schema(description = "GST amount (18% of Transaction Fee)", example = "43.56")
    private BigDecimal gstAmount;

    @Schema(description = "Total charges (Transaction Fee + GST)", example = "285.56")
    private BigDecimal totalCharges;

    @Schema(description = "Total amount payable (BUY) or net receivable (SELL)", example = "242285.56")
    private BigDecimal totalAmount;

    // --- Wallet & Holdings Verification ---
    @Schema(description = "User available escrow/wallet balance", example = "1248500.00")
    private BigDecimal availableBalance;

    @Schema(description = "Estimated post-order balance", example = "1006214.44")
    private BigDecimal postOrderBalance;

    @Schema(description = "Whether user has sufficient wallet balance for BUY order", example = "true")
    private Boolean isBalanceSufficient;

    @Schema(description = "User currently available shares of this company for SELL order", example = "3500")
    private Long availableShares;

    @Schema(description = "Whether user has sufficient shares for SELL order", example = "true")
    private Boolean isSharesSufficient;

    @Schema(description = "Validation status message", example = "Sufficient balance to place this order.")
    private String validationMessage;

    @Schema(description = "Whether the order is ready to place (sufficient balance/shares)", example = "true")
    private Boolean canProceed;
}
