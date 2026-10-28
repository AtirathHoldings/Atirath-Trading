package com.atirath.trading.modules.escrow.dto;

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
public class EscrowPipelineDto {
    private String escrowId;
    private String currentStage; // FUND_ESCROW, PAYMENT_VERIFICATION, SHARE_LOCK, SHARE_TRANSFER, COMPLETED
    private List<TopStepDto> topStepper;
    private EscrowPipelineSummaryDto summary;

    // Stage 1: Fund Escrow
    private FundEscrowStageDto fundEscrow;

    // Stage 2: Payment Verification
    private PaymentVerificationStageDto paymentVerification;

    // Stage 3: Share Lock
    private ShareLockStageDto shareLock;

    // Stage 4: Share Transfer
    private ShareTransferStageDto shareTransfer;

    // Stage 5a: Settlement In Progress (Image 14)
    private SettlementInProgressStageDto settlementInProgress;

    // Stage 5b: Settlement Completed (Image 13)
    private SettlementCompletedStageDto settlementCompleted;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopStepDto {
        private int stepNumber;
        private String title;
        private String subtext;
        private String status; // COMPLETED, ACTIVE, PENDING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowPipelineSummaryDto {
        private String escrowId;
        private String statusBadge;
        private String statusBadgeColor;
        private String tradeId;
        private String negotiationId;
        private String companyName;
        private String companySymbol;
        private String buyerName;
        private String buyerMemberId;
        private String sellerName;
        private String sellerMemberId;
        private Long quantity;
        private BigDecimal pricePerShare;
        private BigDecimal totalEscrowAmount;
        private String totalEscrowAmountFormatted;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MicroStepDto {
        private String title;
        private String timestamp;
        private String status; // COMPLETED, IN_PROGRESS, PENDING
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundEscrowStageDto {
        private String selectedMethod;
        private BigDecimal amount;
        private List<PaymentMethodOptionDto> paymentMethods;
        private List<String> whyEscrowPoints;
        private List<String> importantInformation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethodOptionDto {
        private String id; // UPI, NET_BANKING, DEBIT_CARD
        private String title;
        private String description;
        private String extraNote;
        private boolean recommended;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentVerificationStageDto {
        private String paymentId;
        private String paymentMethod;
        private BigDecimal amount;
        private String initiatedOn;
        private String gatewayReference;
        private String status;
        private List<MicroStepDto> microProgress;
        private List<String> notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareLockStageDto {
        private String paymentId;
        private BigDecimal paymentConfirmedAmount;
        private SellerShareholdingDto sellerShareholding;
        private List<MicroStepDto> microProgress;
        private List<String> whatHappensNext;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SellerShareholdingDto {
        private String dematAccount;
        private Long availableShares;
        private Long sharesToLock;
        private Long postLockBalance;
        private String lockType;
        private String expectedLockTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShareTransferStageDto {
        private String lockReference;
        private Long lockedShares;
        private String companyName;
        private DematPartyDto fromSeller;
        private DematPartyDto toBuyer;
        private DepositoryTransferDetailsDto transferDetails;
        private List<MicroStepDto> microProgress;
        private List<String> whatHappensNext;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DematPartyDto {
        private String memberName;
        private String memberId;
        private String dematNumber;
        private String depository; // NSDL, CDSL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositoryTransferDetailsDto {
        private String transferId;
        private String initiatedOn;
        private String fromDemat;
        private String toDemat;
        private Long quantity;
        private String transferType;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementInProgressStageDto {
        private String estimatedSettlementTime;
        private List<MicroStepDto> overallProgress;
        private List<MicroStepDto> processingChecklist;
        private List<String> pleaseNote;
        private SettlementDetailsDto settlementDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementDetailsDto {
        private String settlementId;
        private BigDecimal expectedPayoutAmount;
        private String payoutBankAccount;
        private String initiatedOn;
        private String status;
        private String estimatedCompletion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettlementCompletedStageDto {
        private String settlementId;
        private String settlementDateTime;
        private BigDecimal payoutAmount;
        private String payoutBankAccount;
        private String status;
        private String referenceNo;
        private Long sharesTraded;
        private BigDecimal totalTradeValue;
        private String completedOn;
        private List<String> whatsNext;
    }
}

