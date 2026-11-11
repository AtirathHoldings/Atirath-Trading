package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.common.ApiException;
import com.atirath.trading.common.ResourceNotFoundException;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.model.ShareTransfer;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.market.repository.ShareTransferRepository;
import com.atirath.trading.modules.portfolio.dto.MemberTransactionsDto;
import com.atirath.trading.modules.portfolio.dto.MemberTransactionsDto.ActivityItemDto;
import com.atirath.trading.modules.portfolio.dto.MemberTransactionsDto.TransactionInsightsDto;
import com.atirath.trading.modules.portfolio.dto.MemberTransactionsDto.TransactionRowDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Read-only service aggregating real Buy, Sell, and Share Transfer transactions
 * for the authenticated user on the Recent Transactions screen (/transactions).
 */
@Service
@RequiredArgsConstructor
public class MemberTransactionsService {

    private static final DateTimeFormatter HEADER_DT_FMT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy | hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter ROW_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter ROW_TS_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);
    private static final List<String> STANDARD_STATUSES =
            List.of("Completed", "Settled", "In Escrow", "Pending", "Open", "Cancelled", "Rejected");

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final MarketCompanyRepository companyRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;
    private final ShareTransferRepository shareTransferRepository;

    @Transactional(readOnly = true)
    public MemberTransactionsDto getMemberTransactions(Long userId,
                                                       String period,
                                                       String startDate,
                                                       String endDate,
                                                       String company,
                                                       String type,
                                                       String status,
                                                       String search,
                                                       int page,
                                                       int size) {
        if (userId == null) {
            throw new ApiException("Authentication required to load transactions", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        String fullName = dataHelper.resolveUserFullName(user);
        OffsetDateTime now = OffsetDateTime.now();
        String normalizedPeriod = normalizeTxPeriod(period);

        List<TimedTransaction> allRecords = collectAllUserTransactions(userId, now);

        // Date window used both for KPI/Insights summary and baseline list
        LocalDate parsedStart = parseIsoDate(startDate);
        LocalDate parsedEnd = parseIsoDate(endDate);
        LocalDate effectiveStart = parsedStart != null ? parsedStart : cutoffForPeriod(normalizedPeriod, now.toLocalDate());

        List<TimedTransaction> periodRecords = allRecords.stream()
                .filter(rec -> matchesDateRange(rec.localDate(), effectiveStart, parsedEnd))
                .toList();

        // KPI & Insights metrics across the active time window
        long buyCount = 0;
        BigDecimal buyAmount = BigDecimal.ZERO;
        long sellCount = 0;
        BigDecimal sellAmount = BigDecimal.ZERO;
        long transferCount = 0;
        BigDecimal transferAmount = BigDecimal.ZERO;

        for (TimedTransaction rec : periodRecords) {
            TransactionRowDto row = rec.row();
            BigDecimal amt = row.getAmount() != null ? row.getAmount() : BigDecimal.ZERO;
            switch (row.getType()) {
                case "Buy" -> {
                    buyCount++;
                    buyAmount = buyAmount.add(amt);
                }
                case "Sell" -> {
                    sellCount++;
                    sellAmount = sellAmount.add(amt);
                }
                case "Transfer" -> {
                    transferCount++;
                    transferAmount = transferAmount.add(amt);
                }
                default -> {
                    // ignore unknown types
                }
            }
        }

        long totalSummaryCount = buyCount + sellCount + transferCount;
        TransactionInsightsDto insights = TransactionInsightsDto.builder()
                .totalCount(totalSummaryCount)
                .buyCount(buyCount)
                .buyPercent(toRoundedPercent(buyCount, totalSummaryCount))
                .sellCount(sellCount)
                .sellPercent(toRoundedPercent(sellCount, totalSummaryCount))
                .transferCount(transferCount)
                .transferPercent(toRoundedPercent(transferCount, totalSummaryCount))
                .build();

        // Apply attribute & search filters
        List<TransactionRowDto> filteredRows = periodRecords.stream()
                .map(TimedTransaction::row)
                .filter(row -> matchesAttributeFilters(row, company, type, status, search))
                .toList();

        int pageSize = size > 0 ? Math.min(size, 100) : 10;
        int totalElements = filteredRows.size();
        int totalPages = totalElements == 0 ? 1 : (int) Math.ceil((double) totalElements / pageSize);
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int fromIdx = (currentPage - 1) * pageSize;
        int toIdx = Math.min(fromIdx + pageSize, totalElements);
        List<TransactionRowDto> pagedRows = fromIdx < totalElements ? filteredRows.subList(fromIdx, toIdx) : List.of();

        Set<String> companyOptions = new LinkedHashSet<>();
        for (MarketCompany mc : companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE")) {
            companyOptions.add(mc.getCompanyName());
        }
        for (TimedTransaction rec : allRecords) {
            if (rec.row().getCompany() != null && !rec.row().getCompany().isBlank()) {
                companyOptions.add(rec.row().getCompany());
            }
        }

        List<ActivityItemDto> recentActivity = buildRecentActivityFeed(allRecords, now);

        return MemberTransactionsDto.builder()
                .userId(userId)
                .memberId("ATG" + userId)
                .userName(fullName)
                .asOfFormatted(now.format(HEADER_DT_FMT))
                .activePeriod(normalizedPeriod)
                .periodSubtitle(buildPeriodSubtitle(normalizedPeriod, parsedStart, parsedEnd))
                .totalTransactions(totalSummaryCount)
                .buyCount(buyCount)
                .buyAmount(buyAmount.setScale(2, RoundingMode.HALF_UP))
                .formattedBuyAmount(dataHelper.formatInr(buyAmount))
                .sellCount(sellCount)
                .sellAmount(sellAmount.setScale(2, RoundingMode.HALF_UP))
                .formattedSellAmount(dataHelper.formatInr(sellAmount))
                .transferCount(transferCount)
                .transferAmount(transferAmount.setScale(2, RoundingMode.HALF_UP))
                .formattedTransferAmount(dataHelper.formatInr(transferAmount))
                .insights(insights)
                .availableCompanies(new ArrayList<>(companyOptions))
                .availableStatuses(STANDARD_STATUSES)
                .transactions(pagedRows)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .recentActivity(recentActivity)
                .build();
    }

    @Transactional(readOnly = true)
    public String buildTransactionsCsv(Long userId,
                                       String period,
                                       String startDate,
                                       String endDate,
                                       String company,
                                       String type,
                                       String status,
                                       String search) {
        if (userId == null) {
            throw new ApiException("Authentication required to export transactions", HttpStatus.UNAUTHORIZED);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));

        OffsetDateTime now = OffsetDateTime.now();
        String normalizedPeriod = normalizeTxPeriod(period);
        LocalDate parsedStart = parseIsoDate(startDate);
        LocalDate parsedEnd = parseIsoDate(endDate);
        LocalDate effectiveStart = parsedStart != null ? parsedStart : cutoffForPeriod(normalizedPeriod, now.toLocalDate());

        List<TransactionRowDto> rows = collectAllUserTransactions(userId, now).stream()
                .filter(rec -> matchesDateRange(rec.localDate(), effectiveStart, parsedEnd))
                .map(TimedTransaction::row)
                .filter(row -> matchesAttributeFilters(row, company, type, status, search))
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("Account Holder,").append(escapeCsv(dataHelper.resolveUserFullName(user))).append('\n');
        sb.append("Member ID,ATG").append(userId).append('\n');
        sb.append("Date,Company,Symbol,Type,Shares,Price (INR),Amount (INR),Status,Reference ID\n");

        if (rows.isEmpty()) {
            sb.append("No transactions matching selected filters,-,-,0,0,0,-,NONE\n");
        } else {
            for (TransactionRowDto row : rows) {
                sb.append(escapeCsv(row.getDate())).append(',')
                        .append(escapeCsv(row.getCompany())).append(',')
                        .append(escapeCsv(row.getSymbol())).append(',')
                        .append(escapeCsv(row.getType())).append(',')
                        .append(row.getShares()).append(',')
                        .append(row.getPrice() != null ? row.getPrice().toPlainString() : "-").append(',')
                        .append(row.getAmount() != null && row.getAmount().compareTo(BigDecimal.ZERO) > 0
                                ? row.getAmount().toPlainString() : "-").append(',')
                        .append(escapeCsv(row.getStatus())).append(',')
                        .append(escapeCsv(row.getReferenceId())).append('\n');
            }
        }
        return sb.toString();
    }

    private List<TimedTransaction> collectAllUserTransactions(Long userId, OffsetDateTime now) {
        List<TimedTransaction> all = new ArrayList<>();
        Set<Long> coveredOrderIds = new HashSet<>();

        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(userId);
        for (MarketOrder o : orders) {
            if (o.getOrderId() != null) {
                coveredOrderIds.add(o.getOrderId());
            }
            boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
            String type = isBuy ? "Buy" : "Sell";
            OffsetDateTime dt = o.getPlacedAt() != null ? o.getPlacedAt() : now;
            BigDecimal price = o.getPricePerShare() != null ? o.getPricePerShare().setScale(2, RoundingMode.HALF_UP) : null;
            BigDecimal amount = o.getOrderValue() != null ? o.getOrderValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            String compName = o.getCompany() != null ? o.getCompany().getCompanyName() : "Atirath Group";
            String symbol = o.getCompany() != null ? o.getCompany().getSymbol() : "";
            long shares = o.getQuantity() != null ? o.getQuantity() : 0L;

            all.add(new TimedTransaction(dt, dt.toLocalDate(), TransactionRowDto.builder()
                    .uid("ORD-" + o.getOrderId())
                    .referenceId(o.getOrderNo())
                    .dateIso(dt.toLocalDate().toString())
                    .date(dt.format(ROW_DATE_FMT))
                    .timestampFormatted(dt.format(ROW_TS_FMT))
                    .companyId(o.getCompany() != null ? o.getCompany().getCompanyId() : null)
                    .company(compName)
                    .symbol(symbol)
                    .type(type)
                    .shares(shares)
                    .price(price)
                    .formattedPrice(formatNumberCell(price))
                    .amount(amount)
                    .formattedAmount(formatNumberCell(amount))
                    .status(normalizeOrderStatus(o.getStatus()))
                    .source("ORDER")
                    .detailsSummary((o.getOrderType() != null ? o.getOrderType() : "LIMIT")
                            + " • " + (o.getValidity() != null ? o.getValidity() : "Day Order"))
                    .build()));
        }

        List<MarketTrade> trades = tradeRepository.findUserTrades(userId);
        for (MarketTrade t : trades) {
            boolean isBuyer = t.getBuyerUser() != null && userId.equals(t.getBuyerUser().getUserId());
            Long matchingOrderId = isBuyer ? t.getBuyOrderId() : t.getSellOrderId();
            if (matchingOrderId != null && coveredOrderIds.contains(matchingOrderId)) {
                continue;
            }
            OffsetDateTime dt = t.getExecutedAt() != null ? t.getExecutedAt() : now;
            BigDecimal price = t.getTradePrice() != null ? t.getTradePrice().setScale(2, RoundingMode.HALF_UP) : null;
            BigDecimal amount = t.getTradeValue() != null ? t.getTradeValue().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            String compName = t.getCompany() != null ? t.getCompany().getCompanyName() : "Atirath Group";
            String symbol = t.getCompany() != null ? t.getCompany().getSymbol() : "";
            long shares = t.getQuantity() != null ? t.getQuantity() : 0L;

            all.add(new TimedTransaction(dt, dt.toLocalDate(), TransactionRowDto.builder()
                    .uid("TRD-" + t.getTradeId())
                    .referenceId(t.getTradeNo())
                    .dateIso(dt.toLocalDate().toString())
                    .date(dt.format(ROW_DATE_FMT))
                    .timestampFormatted(dt.format(ROW_TS_FMT))
                    .companyId(t.getCompany() != null ? t.getCompany().getCompanyId() : null)
                    .company(compName)
                    .symbol(symbol)
                    .type(isBuyer ? "Buy" : "Sell")
                    .shares(shares)
                    .price(price)
                    .formattedPrice(formatNumberCell(price))
                    .amount(amount)
                    .formattedAmount(formatNumberCell(amount))
                    .status("Settled")
                    .source("TRADE")
                    .detailsSummary("Executed & Settled Trade")
                    .build()));
        }

        Set<Long> seenTransferIds = new HashSet<>();
        List<ShareTransfer> transfers = new ArrayList<>();
        transfers.addAll(shareTransferRepository.findByFromUser_UserIdOrderByTransferredAtDesc(userId));
        transfers.addAll(shareTransferRepository.findByToUser_UserIdOrderByTransferredAtDesc(userId));

        for (ShareTransfer st : transfers) {
            if (!seenTransferIds.add(st.getTransferId())) {
                continue;
            }
            boolean isOutgoing = st.getFromUser() != null && userId.equals(st.getFromUser().getUserId());
            OffsetDateTime dt = st.getTransferredAt();
            if (dt == null && st.getTrade() != null) {
                dt = st.getTrade().getExecutedAt();
            }
            if (dt == null) {
                dt = now;
            }
            BigDecimal price = st.getTrade() != null && st.getTrade().getTradePrice() != null
                    ? st.getTrade().getTradePrice().setScale(2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal amount = BigDecimal.ZERO;
            if (st.getTrade() != null && st.getTrade().getTradeValue() != null) {
                amount = st.getTrade().getTradeValue().setScale(2, RoundingMode.HALF_UP);
            }
            String compName = st.getCompany() != null ? st.getCompany().getCompanyName() : "Atirath Group";
            String symbol = st.getCompany() != null ? st.getCompany().getSymbol() : "";
            long shares = st.getQuantity() != null ? st.getQuantity() : 0L;

            all.add(new TimedTransaction(dt, dt.toLocalDate(), TransactionRowDto.builder()
                    .uid("TRF-" + st.getTransferId())
                    .referenceId("TRF-" + st.getTransferId())
                    .dateIso(dt.toLocalDate().toString())
                    .date(dt.format(ROW_DATE_FMT))
                    .timestampFormatted(dt.format(ROW_TS_FMT))
                    .companyId(st.getCompany() != null ? st.getCompany().getCompanyId() : null)
                    .company(compName)
                    .symbol(symbol)
                    .type("Transfer")
                    .shares(shares)
                    .price(price)
                    .formattedPrice(formatNumberCell(price))
                    .amount(amount)
                    .formattedAmount(amount.compareTo(BigDecimal.ZERO) > 0 ? formatNumberCell(amount) : "-")
                    .status(normalizeTransferStatus(st.getTransferStatus()))
                    .source("TRANSFER")
                    .detailsSummary(isOutgoing ? "Outgoing Share Transfer" : "Incoming Share Transfer")
                    .build()));
        }

        all.sort(Comparator.comparing(TimedTransaction::timestamp).reversed());
        return all;
    }

    private List<ActivityItemDto> buildRecentActivityFeed(List<TimedTransaction> records, OffsetDateTime now) {
        return records.stream().limit(4).map(rec -> {
            TransactionRowDto row = rec.row();
            String title;
            String activityType;
            String desc = dataHelper.formatCount(row.getShares()) + " shares of " + row.getCompany();

            if ("Transfer".equals(row.getType())) {
                title = "Completed".equalsIgnoreCase(row.getStatus()) ? "Transfer Completed" : "Share Transfer Initiated";
                activityType = "transfer";
            } else if ("Settled".equalsIgnoreCase(row.getStatus())) {
                title = "Payment Settled";
                activityType = "settle";
                if (row.getAmount() != null && row.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    desc = dataHelper.formatInr(row.getAmount()) + " • " + row.getCompany();
                }
            } else if ("Buy".equals(row.getType())) {
                title = "Completed".equalsIgnoreCase(row.getStatus()) ? "Buy Order Executed" : "Buy Order " + row.getStatus();
                activityType = "buy";
            } else {
                title = "Completed".equalsIgnoreCase(row.getStatus()) ? "Sell Order Executed" : "Sell Order " + row.getStatus();
                activityType = "sell";
            }

            return ActivityItemDto.builder()
                    .id(row.getUid())
                    .title(title)
                    .description(desc)
                    .relativeTime(formatRelativeTime(rec.timestamp(), now))
                    .activityType(activityType)
                    .build();
        }).toList();
    }

    private boolean matchesDateRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return true;
        if (start != null && date.isBefore(start)) return false;
        if (end != null && date.isAfter(end)) return false;
        return true;
    }

    private boolean matchesAttributeFilters(TransactionRowDto row, String company, String type, String status, String search) {
        if (company != null && !company.isBlank() && !"ALL".equalsIgnoreCase(company) && !"All Companies".equalsIgnoreCase(company)) {
            if (row.getCompany() == null || !row.getCompany().equalsIgnoreCase(company.trim())) {
                return false;
            }
        }
        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type) && !"All Types".equalsIgnoreCase(type)) {
            if (!row.getType().equalsIgnoreCase(type.trim())) {
                return false;
            }
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status) && !"All Status".equalsIgnoreCase(status)) {
            if (!row.getStatus().equalsIgnoreCase(status.trim())) {
                return false;
            }
        }
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase(Locale.ENGLISH);
            boolean matchComp = row.getCompany() != null && row.getCompany().toLowerCase(Locale.ENGLISH).contains(q);
            boolean matchSym = row.getSymbol() != null && row.getSymbol().toLowerCase(Locale.ENGLISH).contains(q);
            boolean matchType = row.getType().toLowerCase(Locale.ENGLISH).contains(q);
            boolean matchStatus = row.getStatus().toLowerCase(Locale.ENGLISH).contains(q);
            boolean matchRef = row.getReferenceId() != null && row.getReferenceId().toLowerCase(Locale.ENGLISH).contains(q);
            if (!matchComp && !matchSym && !matchType && !matchStatus && !matchRef) {
                return false;
            }
        }
        return true;
    }

    private String normalizeTxPeriod(String period) {
        if (period == null || period.isBlank()) return "3M";
        return switch (period.trim().toUpperCase(Locale.ROOT)) {
            case "1M", "3M", "6M", "1Y", "ALL" -> period.trim().toUpperCase(Locale.ROOT);
            default -> "3M";
        };
    }

    private LocalDate cutoffForPeriod(String period, LocalDate today) {
        return switch (period) {
            case "1M" -> today.minusMonths(1);
            case "3M" -> today.minusMonths(3);
            case "6M" -> today.minusMonths(6);
            case "1Y" -> today.minusYears(1);
            default -> LocalDate.MIN;
        };
    }

    private String buildPeriodSubtitle(String period, LocalDate start, LocalDate end) {
        if (start != null || end != null) {
            return "in selected date range";
        }
        return switch (period) {
            case "1M" -> "in the last 1 month";
            case "6M" -> "in the last 6 months";
            case "1Y" -> "in the last 1 year";
            case "ALL" -> "across all time";
            default -> "in the last 3 months";
        };
    }

    private LocalDate parseIsoDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String normalizeOrderStatus(String raw) {
        if (raw == null || raw.isBlank()) return "Pending";
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "EXECUTED", "COMPLETED" -> "Completed";
            case "SETTLED" -> "Settled";
            case "IN_ESCROW" -> "In Escrow";
            case "PARTIALLY_FILLED" -> "Partial";
            case "CANCELLED" -> "Cancelled";
            case "REJECTED" -> "Rejected";
            case "OPEN" -> "Open";
            default -> "Pending";
        };
    }

    private String normalizeTransferStatus(String raw) {
        if (raw == null || raw.isBlank()) return "Pending";
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "COMPLETED", "SETTLED" -> "Completed";
            case "CANCELLED", "REJECTED" -> "Cancelled";
            default -> "Pending";
        };
    }

    private String formatNumberCell(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "-";
        }
        String withSymbol = dataHelper.formatInr(amount);
        return withSymbol.startsWith("₹ ") ? withSymbol.substring(2) : withSymbol;
    }

    private int toRoundedPercent(long count, long total) {
        if (total <= 0) return 0;
        return (int) Math.round((count * 100.0) / total);
    }

    private String formatRelativeTime(OffsetDateTime dt, OffsetDateTime now) {
        long minutes = Math.max(0, Duration.between(dt, now).toMinutes());
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days <= 30) return days + "d ago";
        return dt.format(ROW_DATE_FMT);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private record TimedTransaction(OffsetDateTime timestamp, LocalDate localDate, TransactionRowDto row) {}
}

