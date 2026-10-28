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
public class EscrowDetailDto {
    private String escrowId;
    private String status;
    private String statusDescription;
    private String createdOn;

    private String tradeId;
    private String negotiationId;
    private String agreementId;

    private CompanyInfoDto company;
    private PartyDto buyer;
    private PartyDto seller;
    private TradeAndEscrowInfoDto tradeAndEscrowInfo;
    private EscrowAccountDetailsDto escrowAccountDetails;
    private List<ProgressStepDto> escrowProgress;
    private List<EscrowDocumentDto> documents;
    private List<ActivityEventDto> recentActivity;
    private EscrowActionsDto actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyInfoDto {
        private String companyName;
        private String symbol;
        private String logoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartyDto {
        private String memberName;
        private String initials;
        private boolean isCurrentUser;
        private boolean verified;
        private String memberId;
        private String email;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeAndEscrowInfoDto {
        private String company;
        private String securityType;
        private Long quantity;
        private BigDecimal agreedPricePerShare;
        private BigDecimal totalTradeValue;
        private String escrowAmount;
        private String paymentMode;
        private String expectedSettlement;
        private String escrowCreatedOn;
        private String validTill;
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowAccountDetailsDto {
        private String bankName;
        private String accountName;
        private String accountNumber;
        private String ifscCode;
        private String accountType;
        private String branch;
        private String referenceNo;
        private BigDecimal amountToTransfer;
        private String instructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressStepDto {
        private int stepNumber;
        private String title;
        private String subtext;
        private String status;
        private boolean completed;
        private boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowDocumentDto {
        private String title;
        private String fileSize;
        private String date;
        private String format;
        private String downloadUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityEventDto {
        private String title;
        private String timestamp;
        private String description;
        private String dotColor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EscrowActionsDto {
        private boolean canTransferFunds;
        private boolean canViewAgreement;
        private boolean canContactSupport;
    }
}
