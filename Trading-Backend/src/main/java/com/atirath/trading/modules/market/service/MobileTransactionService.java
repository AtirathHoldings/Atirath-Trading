package com.atirath.trading.modules.market.service;

import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.dto.mobile.MobileTransactionItemDto;
import com.atirath.trading.modules.market.dto.mobile.MobileTransactionsPageDto;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import com.atirath.trading.modules.portfolio.service.PortfolioDataHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileTransactionService {

    private static final DateTimeFormatter TXN_DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    private final PortfolioDataHelper dataHelper;
    private final UserAccountRepository userAccountRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketTradeRepository tradeRepository;

    @Transactional(readOnly = true)
    public MobileTransactionsPageDto getMobileTransactions(Long userId, String filter) {
        UserAccount user = null;
        if (userId != null) {
            user = userAccountRepository.findById(userId).orElse(null);
        }

        if (user == null) {
            return MobileTransactionsPageDto.builder()
                    .totalCount(0)
                    .transactions(List.of())
                    .build();
        }

        Long uid = user.getUserId();
        List<MobileTransactionItemDto> list = new ArrayList<>();

        // 1. Orders
        List<MarketOrder> orders = orderRepository.findByUserAccount_UserIdOrderByPlacedAtDesc(uid);
        for (MarketOrder o : orders) {
            if (o.getCompany() == null) continue;
            boolean isBuy = "BUY".equalsIgnoreCase(o.getOrderSide());
            String type = isBuy ? "buy" : "sell";
            String sym = o.getCompany().getSymbol();
            String cname = o.getCompany().getCompanyName();
            int qty = o.getQuantity() != null ? o.getQuantity().intValue() : 0;
            double price = o.getPricePerShare() != null ? o.getPricePerShare().doubleValue() : 0.0;
            BigDecimal val = o.getOrderValue() != null && o.getOrderValue().compareTo(BigDecimal.ZERO) > 0
                    ? o.getOrderValue()
                    : BigDecimal.valueOf(qty * price);
            String dt = o.getPlacedAt() != null ? o.getPlacedAt().format(TXN_DISPLAY_FMT) : "-";
            String status = "FILLED".equalsIgnoreCase(o.getStatus()) ? "settled"
                    : ("OPEN".equalsIgnoreCase(o.getStatus()) ? "pending" : o.getStatus().toLowerCase(Locale.ROOT));

            String ref = (o.getOrderNo() != null && !o.getOrderNo().isBlank())
                    ? o.getOrderNo()
                    : "ORD-" + o.getOrderId();

            list.add(MobileTransactionItemDto.builder()
                    .id(ref)
                    .orderId(o.getOrderId())
                    .type(type)
                    .company(sym)
                    .companyName(cname)
                    .shares(qty)
                    .price(price)
                    .total(val.doubleValue())
                    .formattedTotal(dataHelper.formatInr(val))
                    .date(dt)
                    .status(status)
                    .isBuy(isBuy)
                    .build());
        }

        // 2. Trades (settlements)
        List<MarketTrade> trades = tradeRepository.findUserTrades(uid);
        for (MarketTrade t : trades) {
            if (t.getCompany() == null) continue;
            boolean isBuyer = t.getBuyerUser() != null && uid.equals(t.getBuyerUser().getUserId());
            String sym = t.getCompany().getSymbol();
            String cname = t.getCompany().getCompanyName();
            int qty = t.getQuantity() != null ? t.getQuantity().intValue() : 0;
            double price = t.getTradePrice() != null ? t.getTradePrice().doubleValue() : 0.0;
            BigDecimal val = t.getTradeValue() != null ? t.getTradeValue() : BigDecimal.valueOf(qty * price);
            String dt = t.getExecutedAt() != null ? t.getExecutedAt().format(TXN_DISPLAY_FMT) : "-";
            String status = "settled";

            String ref = (t.getTradeNo() != null && !t.getTradeNo().isBlank())
                    ? t.getTradeNo()
                    : "TRD-" + t.getTradeId();

            list.add(MobileTransactionItemDto.builder()
                    .id(ref)
                    .orderId(isBuyer ? t.getBuyOrderId() : t.getSellOrderId())
                    .type(isBuyer ? "buy" : "sell")
                    .company(sym)
                    .companyName(cname)
                    .shares(qty)
                    .price(price)
                    .total(val.doubleValue())
                    .formattedTotal(dataHelper.formatInr(val))
                    .date(dt)
                    .status(status)
                    .isBuy(isBuyer)
                    .build());
        }

        // Filter if requested
        if (filter != null && !filter.isBlank() && !"all".equalsIgnoreCase(filter.trim())) {
            String f = filter.trim().toLowerCase(Locale.ROOT);
            list = list.stream()
                    .filter(t -> t.getType().equalsIgnoreCase(f) || t.getStatus().equalsIgnoreCase(f))
                    .toList();
        }

        return MobileTransactionsPageDto.builder()
                .totalCount(list.size())
                .transactions(list)
                .build();
    }
}
