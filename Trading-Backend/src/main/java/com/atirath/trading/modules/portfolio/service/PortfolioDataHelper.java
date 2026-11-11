package com.atirath.trading.modules.portfolio.service;

import com.atirath.trading.common.AccountType;
import com.atirath.trading.modules.auth.model.CorporateProfile;
import com.atirath.trading.modules.auth.model.IndividualProfile;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.CorporateProfileRepository;
import com.atirath.trading.modules.auth.repository.IndividualProfileRepository;
import com.atirath.trading.modules.market.dto.MarketTickDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketPriceHistory;
import com.atirath.trading.modules.market.model.UserShareHolding;
import com.atirath.trading.modules.market.repository.MarketPriceHistoryRepository;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Shared read-only calculation and formatting helper for Member Dashboard and Portfolio screens.
 */
@Component
@RequiredArgsConstructor
public class PortfolioDataHelper {

    public static final List<String> CHART_COLORS = List.of(
            "#1e66f5", "#2bb673", "#f6a609", "#9b6df5",
            "#b774f4", "#ef4444", "#06b6d4", "#ec4899"
    );

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ONE_LAKH = new BigDecimal("100000");
    private static final BigDecimal ONE_CRORE = new BigDecimal("10000000");
    private static final DateTimeFormatter CHART_DATE_FMT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    private final IndividualProfileRepository individualProfileRepository;
    private final CorporateProfileRepository corporateProfileRepository;
    private final MarketPriceHistoryRepository priceHistoryRepository;
    private final MarketBroadcastService broadcastService;

    public String resolveUserFullName(UserAccount user) {
        if (user.getAccountType() == AccountType.CORPORATE) {
            Optional<CorporateProfile> corp = corporateProfileRepository.findByUserAccount_UserId(user.getUserId());
            if (corp.isPresent()) {
                if (corp.get().getContactPersonName() != null && !corp.get().getContactPersonName().isBlank()) {
                    return corp.get().getContactPersonName().trim();
                }
                if (corp.get().getCompanyName() != null && !corp.get().getCompanyName().isBlank()) {
                    return corp.get().getCompanyName().trim();
                }
            }
        } else {
            Optional<IndividualProfile> indiv = individualProfileRepository.findByUserAccount_UserId(user.getUserId());
            if (indiv.isPresent() && indiv.get().getFullName() != null && !indiv.get().getFullName().isBlank()) {
                return indiv.get().getFullName().trim();
            }
        }
        String email = user.getEmail();
        if (email != null && email.contains("@")) {
            String local = email.substring(0, email.indexOf('@'));
            if (!local.isEmpty()) {
                return Character.toUpperCase(local.charAt(0)) + local.substring(1);
            }
        }
        return "Member";
    }

    public String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Member";
        }
        String trimmed = fullName.trim();
        int spaceIdx = trimmed.indexOf(' ');
        return spaceIdx > 0 ? trimmed.substring(0, spaceIdx) : trimmed;
    }

    public String extractInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "MG";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ENGLISH);
        }
        String single = parts[0];
        return single.substring(0, Math.min(2, single.length())).toUpperCase(Locale.ENGLISH);
    }

    public HoldingSummary aggregateHoldings(List<UserShareHolding> rawHoldings) {
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal investedAmount = BigDecimal.ZERO;
        BigDecimal prevTotalValue = BigDecimal.ZERO;
        long totalShares = 0L;
        long totalLockedShares = 0L;
        List<ComputedHolding> items = new ArrayList<>();

        for (UserShareHolding h : rawHoldings) {
            MarketCompany comp = h.getCompany();
            long shares = h.getTotalShares();
            long availableShares = h.getAvailableShares() != null ? h.getAvailableShares() : shares;
            long lockedShares = h.getLockedShares() != null ? h.getLockedShares() : 0L;
            BigDecimal avgPrice = h.getAverageBuyPrice() != null
                    ? h.getAverageBuyPrice().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            PriceQuote quote = resolvePriceQuote(comp, avgPrice);
            BigDecimal shareCount = BigDecimal.valueOf(shares);
            BigDecimal currentVal = quote.currentPrice().multiply(shareCount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal investedVal = avgPrice.multiply(shareCount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal prevVal = quote.previousClose().multiply(shareCount).setScale(2, RoundingMode.HALF_UP);

            BigDecimal pnl = currentVal.subtract(investedVal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnlPct = computePercent(pnl, investedVal);

            totalValue = totalValue.add(currentVal);
            investedAmount = investedAmount.add(investedVal);
            prevTotalValue = prevTotalValue.add(prevVal);
            totalShares += shares;
            totalLockedShares += lockedShares;

            items.add(new ComputedHolding(
                    comp,
                    shares,
                    availableShares,
                    lockedShares,
                    avgPrice,
                    quote.currentPrice(),
                    quote.previousClose(),
                    currentVal,
                    investedVal,
                    pnl,
                    pnlPct
            ));
        }

        items.sort(Comparator.comparing(ComputedHolding::currentValue).reversed());

        return new HoldingSummary(
                items,
                totalValue.setScale(2, RoundingMode.HALF_UP),
                investedAmount.setScale(2, RoundingMode.HALF_UP),
                prevTotalValue.setScale(2, RoundingMode.HALF_UP),
                totalShares,
                totalLockedShares
        );
    }

    public PriceQuote resolvePriceQuote(MarketCompany comp, BigDecimal fallback) {
        MarketTickDto tick = comp.getSymbol() != null ? broadcastService.getTick(comp.getSymbol()) : null;
        if (tick != null && tick.getCurrentPrice() != null) {
            BigDecimal current = tick.getCurrentPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal prev = tick.getPreviousClose() != null
                    ? tick.getPreviousClose().setScale(2, RoundingMode.HALF_UP)
                    : current;
            return new PriceQuote(current, prev);
        }

        Optional<MarketPriceHistory> latestOpt = priceHistoryRepository.findTopByCompany_CompanyIdOrderByPriceTimeDesc(comp.getCompanyId());
        if (latestOpt.isPresent() && latestOpt.get().getClosePrice() != null) {
            BigDecimal current = latestOpt.get().getClosePrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal open = latestOpt.get().getOpenPrice() != null
                    ? latestOpt.get().getOpenPrice().setScale(2, RoundingMode.HALF_UP)
                    : current;
            return new PriceQuote(current, open);
        }

        BigDecimal safe = fallback != null ? fallback.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new PriceQuote(safe, safe);
    }

    public List<TrendSnapshot> buildPerformanceSeries(List<UserShareHolding> rawHoldings,
                                                      BigDecimal currentTotalValue,
                                                      BigDecimal investedAmount,
                                                      BigDecimal prevTotalValue,
                                                      String period,
                                                      OffsetDateTime now) {
        if (rawHoldings.isEmpty() || currentTotalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }

        LocalDate cutoff = cutoffDateForPeriod(period, now.toLocalDate());
        Map<LocalDate, BigDecimal> dateWeightedValues = new TreeMap<>();

        for (UserShareHolding h : rawHoldings) {
            Long companyId = h.getCompany().getCompanyId();
            long shares = h.getTotalShares();
            List<MarketPriceHistory> history = priceHistoryRepository
                    .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(companyId, "DAY");
            if (history.isEmpty()) {
                history = priceHistoryRepository
                        .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(companyId, "INTRADAY");
            }
            Map<LocalDate, BigDecimal> latestPerDay = new TreeMap<>();
            for (MarketPriceHistory ph : history) {
                if (ph.getPriceTime() != null && ph.getClosePrice() != null) {
                    LocalDate day = ph.getPriceTime().toLocalDate();
                    if (!day.isBefore(cutoff)) {
                        latestPerDay.put(day, ph.getClosePrice());
                    }
                }
            }
            for (Map.Entry<LocalDate, BigDecimal> entry : latestPerDay.entrySet()) {
                BigDecimal contribution = entry.getValue().multiply(BigDecimal.valueOf(shares));
                dateWeightedValues.merge(entry.getKey(), contribution, BigDecimal::add);
            }
        }

        Map<String, DatedValue> ordered = new LinkedHashMap<>();
        LocalDate today = now.toLocalDate();

        if (investedAmount.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate startDate = !dateWeightedValues.isEmpty()
                    ? dateWeightedValues.keySet().iterator().next()
                    : today.minusDays(1);
            ordered.put("Cost Basis", new DatedValue(startDate.toString(), investedAmount));
        }

        for (Map.Entry<LocalDate, BigDecimal> entry : dateWeightedValues.entrySet()) {
            if (entry.getKey().equals(today)) {
                continue;
            }
            ordered.put(
                    entry.getKey().format(CHART_DATE_FMT),
                    new DatedValue(entry.getKey().toString(), entry.getValue().setScale(2, RoundingMode.HALF_UP))
            );
        }

        if (ordered.size() <= 1 && prevTotalValue.compareTo(BigDecimal.ZERO) > 0) {
            LocalDate prevDay = today.minusDays(1);
            ordered.put(prevDay.format(CHART_DATE_FMT), new DatedValue(prevDay.toString(), prevTotalValue));
        }
        ordered.put("Current", new DatedValue(today.toString(), currentTotalValue));

        List<TrendSnapshot> points = new ArrayList<>();
        int index = 0;
        int size = ordered.size();
        for (Map.Entry<String, DatedValue> entry : ordered.entrySet()) {
            BigDecimal val = entry.getValue().value().setScale(2, RoundingMode.HALF_UP);
            points.add(new TrendSnapshot(
                    entry.getKey(),
                    entry.getValue().isoDate(),
                    val,
                    formatInr(val),
                    index == size - 1
            ));
            index++;
        }
        return points;
    }

    public BigDecimal computePeriodReturnPct(List<UserShareHolding> rawHoldings,
                                             BigDecimal currentValue,
                                             BigDecimal investedAmount,
                                             LocalDate targetCutoff) {
        if (rawHoldings.isEmpty() || currentValue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal historicalPortfolioValue = BigDecimal.ZERO;
        boolean foundHistorical = false;

        for (UserShareHolding h : rawHoldings) {
            Long companyId = h.getCompany().getCompanyId();
            long shares = h.getTotalShares();
            BigDecimal fallbackUnit = h.getAverageBuyPrice() != null ? h.getAverageBuyPrice() : BigDecimal.ZERO;

            List<MarketPriceHistory> history = priceHistoryRepository
                    .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(companyId, "DAY");
            if (history.isEmpty()) {
                history = priceHistoryRepository
                        .findByCompany_CompanyIdAndIntervalTypeOrderByPriceTimeAsc(companyId, "INTRADAY");
            }

            BigDecimal baselinePrice = null;
            for (MarketPriceHistory ph : history) {
                if (ph.getPriceTime() != null && ph.getClosePrice() != null
                        && !ph.getPriceTime().toLocalDate().isBefore(targetCutoff)) {
                    baselinePrice = ph.getClosePrice();
                    foundHistorical = true;
                    break;
                }
            }
            if (baselinePrice == null && !history.isEmpty() && history.get(0).getClosePrice() != null) {
                baselinePrice = history.get(0).getClosePrice();
                foundHistorical = true;
            }
            if (baselinePrice == null) {
                baselinePrice = fallbackUnit;
            }
            historicalPortfolioValue = historicalPortfolioValue.add(baselinePrice.multiply(BigDecimal.valueOf(shares)));
        }

        BigDecimal baseline = foundHistorical && historicalPortfolioValue.compareTo(BigDecimal.ZERO) > 0
                ? historicalPortfolioValue
                : investedAmount;

        if (baseline.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return computePercent(currentValue.subtract(baseline), baseline);
    }

    private LocalDate cutoffDateForPeriod(String period, LocalDate today) {
        return switch (normalizePeriod(period)) {
            case "1M" -> today.minusMonths(1);
            case "3M" -> today.minusMonths(3);
            case "6M" -> today.minusMonths(6);
            case "1Y" -> today.minusYears(1);
            default -> LocalDate.MIN;
        };
    }

    public String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "1M";
        }
        return switch (period.trim().toUpperCase(Locale.ROOT)) {
            case "1M", "3M", "6M", "1Y", "ALL" -> period.trim().toUpperCase(Locale.ROOT);
            default -> "1M";
        };
    }

    public BigDecimal computePercent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerator.multiply(ONE_HUNDRED).divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public String formatSignedPercent(BigDecimal pct) {
        BigDecimal safe = pct != null ? pct.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        String prefix = safe.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return prefix + safe.toPlainString() + "%";
    }

    public String formatSignedInr(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "₹ 0";
        }
        String formatted = formatInr(amount.abs());
        return amount.compareTo(BigDecimal.ZERO) < 0 ? "-" + formatted : formatted;
    }

    public String formatSignedInrWithPlus(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "₹ 0";
        }
        String formatted = formatInr(amount.abs());
        return amount.compareTo(BigDecimal.ZERO) < 0 ? "- " + formatted : "+ " + formatted;
    }

    public String formatInr(BigDecimal amount) {
        if (amount == null) {
            return "₹ 0";
        }
        boolean negative = amount.compareTo(BigDecimal.ZERO) < 0;
        BigDecimal abs = amount.abs().setScale(2, RoundingMode.HALF_UP);
        BigDecimal stripped = abs.stripTrailingZeros();
        String sign = negative ? "-" : "";
        if (stripped.scale() <= 0) {
            return sign + "₹ " + groupIndianDigits(abs.toBigInteger().toString());
        }
        String plain = abs.toPlainString();
        int dot = plain.indexOf('.');
        String intPart = plain.substring(0, dot);
        String fracPart = plain.substring(dot);
        return sign + "₹ " + groupIndianDigits(intPart) + fracPart;
    }

    public String formatCompactInr(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "₹ 0";
        }
        BigDecimal abs = amount.abs();
        String sign = amount.compareTo(BigDecimal.ZERO) < 0 ? "-" : "";
        if (abs.compareTo(ONE_CRORE) >= 0) {
            return sign + "₹ " + abs.divide(ONE_CRORE, 2, RoundingMode.HALF_UP).toPlainString() + "Cr";
        }
        if (abs.compareTo(ONE_LAKH) >= 0) {
            return sign + "₹ " + abs.divide(ONE_LAKH, 2, RoundingMode.HALF_UP).toPlainString() + "L";
        }
        return formatInr(amount);
    }

    public String formatCount(long count) {
        return NumberFormat.getNumberInstance(new Locale("en", "IN")).format(count);
    }

    private String groupIndianDigits(String digits) {
        if (digits.length() <= 3) {
            return digits;
        }
        String lastThree = digits.substring(digits.length() - 3);
        String rest = digits.substring(0, digits.length() - 3);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rest.length(); i++) {
            sb.append(rest.charAt(i));
            int remaining = rest.length() - 1 - i;
            if (remaining > 0 && remaining % 2 == 0) {
                sb.append(',');
            }
        }
        return sb.append(',').append(lastThree).toString();
    }

    public record PriceQuote(BigDecimal currentPrice, BigDecimal previousClose) {}

    public record ComputedHolding(MarketCompany company,
                                  long shares,
                                  long availableShares,
                                  long lockedShares,
                                  BigDecimal avgPrice,
                                  BigDecimal currentPrice,
                                  BigDecimal previousClose,
                                  BigDecimal currentValue,
                                  BigDecimal investedValue,
                                  BigDecimal pnl,
                                  BigDecimal pnlPercentage) {}

    public record HoldingSummary(List<ComputedHolding> holdings,
                                 BigDecimal totalValue,
                                 BigDecimal investedAmount,
                                 BigDecimal prevTotalValue,
                                 long totalShares,
                                 long totalLockedShares) {}

    public record TrendSnapshot(String label, String dateIso, BigDecimal value, String formattedValue, boolean isCurrent) {}

    private record DatedValue(String isoDate, BigDecimal value) {}
}

