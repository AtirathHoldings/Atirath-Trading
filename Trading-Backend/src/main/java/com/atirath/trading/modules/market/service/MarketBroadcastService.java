package com.atirath.trading.modules.market.service;

import com.atirath.trading.config.MarketRawWebSocketHandler;
import com.atirath.trading.modules.auth.model.UserAccount;
import com.atirath.trading.modules.auth.repository.UserAccountRepository;
import com.atirath.trading.modules.market.dto.LiveTradeDto;
import com.atirath.trading.modules.market.dto.MarketTickDto;
import com.atirath.trading.modules.market.dto.MarketWebSocketMessage;
import com.atirath.trading.modules.market.dto.OrderBookUpdateDto;
import com.atirath.trading.modules.market.model.MarketCompany;
import com.atirath.trading.modules.market.model.MarketOrder;
import com.atirath.trading.modules.market.model.MarketTrade;
import com.atirath.trading.modules.market.repository.MarketCompanyRepository;
import com.atirath.trading.modules.market.repository.MarketOrderRepository;
import com.atirath.trading.modules.market.repository.MarketTradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MarketRawWebSocketHandler rawWebSocketHandler;
    private final MarketCompanyRepository companyRepository;
    private final MarketTradeRepository tradeRepository;
    private final MarketOrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;

    // Cache of active simulated prices and metrics
    private final Map<String, MarketTickDto> currentTicks = new ConcurrentHashMap<>();
    private final Map<String, OrderBookUpdateDto> currentOrderBooks = new ConcurrentHashMap<>();

    private final java.util.concurrent.atomic.AtomicLong tradeCounter =
            new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis() % 100000000L);
    private long tickSequence = 0;

    @PostConstruct
    public void init() {
        log.info("[MarketBroadcastService] Initializing real-time market broadcaster...");
        refreshCompanies();
    }

    public synchronized void refreshCompanies() {
        List<MarketCompany> companies = companyRepository.findByStatusOrderByCompanyNameAsc("ACTIVE");
        for (MarketCompany comp : companies) {
            String sym = comp.getSymbol().toUpperCase();
            if (!currentTicks.containsKey(sym)) {
                BigDecimal basePrice = getBasePriceForSymbol(sym);
                BigDecimal prevClose = switch (sym) {
                    case "ATG-H" -> new BigDecimal("210.00");
                    case "ATG-M" -> basePrice.multiply(new BigDecimal("1.012")).setScale(2, RoundingMode.HALF_UP);
                    case "ATG-L" -> basePrice.multiply(new BigDecimal("1.007")).setScale(2, RoundingMode.HALF_UP);
                    default -> basePrice.multiply(new BigDecimal("0.992")).setScale(2, RoundingMode.HALF_UP);
                };
                BigDecimal diff = basePrice.subtract(prevClose);
                BigDecimal pct = diff.divide(prevClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                BigDecimal high = "ATG-H".equals(sym) ? new BigDecimal("250.00") : basePrice.multiply(new BigDecimal("1.015")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal low = "ATG-H".equals(sym) ? new BigDecimal("208.00") : basePrice.multiply(new BigDecimal("0.985")).setScale(2, RoundingMode.HALF_UP);
                long vol = "ATG-H".equals(sym) ? 250000L : 35000L;

                MarketTickDto tick = MarketTickDto.builder()
                        .symbol(sym)
                        .companyId(comp.getCompanyId())
                        .companyName(comp.getCompanyName())
                        .currentPrice(basePrice)
                        .previousClose(prevClose)
                        .priceChange(diff)
                        .priceChangePercentage(pct)
                        .highPrice(high)
                        .lowPrice(low)
                        .volumeShares(vol)
                        .turnoverValue(basePrice.multiply(BigDecimal.valueOf(vol)).setScale(2, RoundingMode.HALF_UP))
                        .timestamp(OffsetDateTime.now())
                        .build();

                currentTicks.put(sym, tick);
                currentOrderBooks.put(sym, generateOrderBook(tick));
            }
        }
        log.info("[MarketBroadcastService] Initialized {} tradable companies in memory cache", currentTicks.size());
    }

    private BigDecimal getBasePriceForSymbol(String symbol) {
        return switch (symbol) {
            case "ATG-H" -> new BigDecimal("245.00");
            case "ATG-T" -> new BigDecimal("1240.80");
            case "ATG-E" -> new BigDecimal("312.40");
            case "ATG-F" -> new BigDecimal("580.00");
            case "ATG-L" -> new BigDecimal("425.60");
            case "ATG-M" -> new BigDecimal("189.20");
            case "ATG-R" -> new BigDecimal("765.10");
            default -> new BigDecimal("500.00");
        };
    }

    /**
     * Broadcast live market ticks and order books every 2 seconds
     */
    @Scheduled(fixedRate = 2000)
    public void broadcastMarketPulse() {
        if (currentTicks.isEmpty()) {
            refreshCompanies();
            if (currentTicks.isEmpty()) return;
        }

        tickSequence++;
        List<String> symbols = new ArrayList<>(currentTicks.keySet());
        if (symbols.isEmpty()) return;

        // Choose 1-3 random stocks to update per pulse
        Collections.shuffle(symbols);
        int stocksToUpdate = Math.min(symbols.size(), ThreadLocalRandom.current().nextInt(1, 4));

        for (int i = 0; i < stocksToUpdate; i++) {
            String sym = symbols.get(i);
            MarketTickDto tick = currentTicks.get(sym);
            if (tick == null) continue;

            // Generate small price change (-0.35% to +0.35%)
            double percentChange = (ThreadLocalRandom.current().nextDouble(-0.35, 0.35)) / 100.0;
            BigDecimal oldPrice = tick.getCurrentPrice();
            BigDecimal delta = oldPrice.multiply(BigDecimal.valueOf(percentChange)).setScale(2, RoundingMode.HALF_UP);
            if (delta.compareTo(BigDecimal.ZERO) == 0) {
                delta = ThreadLocalRandom.current().nextBoolean() ? new BigDecimal("0.20") : new BigDecimal("-0.20");
            }
            BigDecimal newPrice = oldPrice.add(delta).setScale(2, RoundingMode.HALF_UP);
            if (newPrice.compareTo(new BigDecimal("5.00")) < 0) {
                newPrice = new BigDecimal("5.00");
            }

            BigDecimal priceChange = newPrice.subtract(tick.getPreviousClose());
            BigDecimal changePct = priceChange.divide(tick.getPreviousClose(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

            BigDecimal high = tick.getHighPrice().max(newPrice);
            BigDecimal low = tick.getLowPrice().min(newPrice);
            long newVolume = tick.getVolumeShares() + ThreadLocalRandom.current().nextInt(20, 150);
            BigDecimal newTurnover = newPrice.multiply(BigDecimal.valueOf(newVolume)).setScale(2, RoundingMode.HALF_UP);

            MarketTickDto updatedTick = MarketTickDto.builder()
                    .symbol(sym)
                    .companyId(tick.getCompanyId())
                    .companyName(tick.getCompanyName())
                    .currentPrice(newPrice)
                    .previousClose(tick.getPreviousClose())
                    .priceChange(priceChange)
                    .priceChangePercentage(changePct)
                    .highPrice(high)
                    .lowPrice(low)
                    .volumeShares(newVolume)
                    .turnoverValue(newTurnover)
                    .timestamp(OffsetDateTime.now())
                    .build();

            currentTicks.put(sym, updatedTick);

            // 1. Broadcast Market Tick via STOMP
            messagingTemplate.convertAndSend("/topic/market", updatedTick);
            messagingTemplate.convertAndSend("/topic/market/" + sym, updatedTick);

            // 2. Broadcast Market Tick via Raw WebSocket
            rawWebSocketHandler.broadcastSymbolEvent(sym, MarketWebSocketMessage.of("MARKET_TICK", updatedTick));

            // 3. Generate & Broadcast Order Book Depth
            OrderBookUpdateDto orderBook = generateOrderBook(updatedTick);
            currentOrderBooks.put(sym, orderBook);

            messagingTemplate.convertAndSend("/topic/orderbook", orderBook);
            messagingTemplate.convertAndSend("/topic/orderbook/" + sym, orderBook);
            rawWebSocketHandler.broadcastSymbolEvent(sym, MarketWebSocketMessage.of("ORDER_BOOK", orderBook));
        }

        // Every ~6 seconds (every 3 ticks), simulate an executed trade
        if (tickSequence % 3 == 0) {
            simulateTrade(symbols.get(0));
        }
    }

    private OrderBookUpdateDto generateOrderBook(MarketTickDto tick) {
        BigDecimal price = tick.getCurrentPrice();
        List<OrderBookUpdateDto.OrderBookLevelDto> bids = new ArrayList<>();
        List<OrderBookUpdateDto.OrderBookLevelDto> asks = new ArrayList<>();

        // Generate 5 bid levels below market price
        BigDecimal currentBid = price.subtract(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        for (int i = 0; i < 5; i++) {
            long qty = ThreadLocalRandom.current().nextLong(50, 950);
            int count = ThreadLocalRandom.current().nextInt(1, 8);
            bids.add(new OrderBookUpdateDto.OrderBookLevelDto(currentBid, qty, count));
            currentBid = currentBid.subtract(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.20, 0.80))).setScale(2, RoundingMode.HALF_UP);
        }

        // Generate 5 ask levels above market price
        BigDecimal currentAsk = price.add(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        for (int i = 0; i < 5; i++) {
            long qty = ThreadLocalRandom.current().nextLong(40, 850);
            int count = ThreadLocalRandom.current().nextInt(1, 8);
            asks.add(new OrderBookUpdateDto.OrderBookLevelDto(currentAsk, qty, count));
            currentAsk = currentAsk.add(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(0.20, 0.80))).setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal spread = asks.get(0).getPrice().subtract(bids.get(0).getPrice()).setScale(2, RoundingMode.HALF_UP);

        return OrderBookUpdateDto.builder()
                .symbol(tick.getSymbol())
                .companyId(tick.getCompanyId())
                .currentPrice(price)
                .spread(spread)
                .bids(bids)
                .asks(asks)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    private void simulateTrade(String sym) {
        MarketTickDto tick = currentTicks.get(sym);
        if (tick == null) return;

        long seq = tradeCounter.incrementAndGet();
        String tradeNo = String.format("TRD-%s-%06d", sym, seq % 1000000);
        long qty = (long) ThreadLocalRandom.current().nextInt(10, 150);
        BigDecimal price = tick.getCurrentPrice();
        BigDecimal tradeVal = price.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
        boolean isBuy = ThreadLocalRandom.current().nextBoolean();

        LiveTradeDto tradeDto = LiveTradeDto.builder()
                .tradeNo(tradeNo)
                .symbol(sym)
                .companyName(tick.getCompanyName())
                .tradePrice(price)
                .quantity(qty)
                .tradeValue(tradeVal)
                .executedAt(OffsetDateTime.now())
                .isBuy(isBuy)
                .build();

        // 1. Broadcast via STOMP
        messagingTemplate.convertAndSend("/topic/trades", tradeDto);
        messagingTemplate.convertAndSend("/topic/trades/" + sym, tradeDto);

        // 2. Broadcast via Raw WebSocket
        rawWebSocketHandler.broadcastSymbolEvent(sym, MarketWebSocketMessage.of("TRADE", tradeDto));

        // 3. Persist to DB in background if company entity is present
        try {
            Optional<MarketCompany> compOpt = companyRepository.findBySymbolIgnoreCase(sym);
            if (compOpt.isPresent()) {
                List<UserAccount> users = userAccountRepository.findAll();
                UserAccount buyer = users.isEmpty() ? null : users.get(ThreadLocalRandom.current().nextInt(users.size()));
                UserAccount seller = users.size() > 1 ? users.stream().filter(u -> buyer == null || !u.getUserId().equals(buyer.getUserId())).findAny().orElse(users.get(0)) : buyer;

                MarketOrder buyOrderBuilder = MarketOrder.builder()
                        .orderNo(String.format("ORD-B-%s-%06d", sym, seq % 1000000))
                        .userAccount(buyer)
                        .company(compOpt.get())
                        .orderSide("BUY")
                        .orderType("LIMIT")
                        .quantity(qty)
                        .pricePerShare(price)
                        .orderValue(tradeVal)
                        .status("EXECUTED")
                        .build();

                MarketOrder sellOrderBuilder = MarketOrder.builder()
                        .orderNo(String.format("ORD-S-%s-%06d", sym, (seq % 1000000) + 1))
                        .userAccount(seller)
                        .company(compOpt.get())
                        .orderSide("SELL")
                        .orderType("LIMIT")
                        .quantity(qty)
                        .pricePerShare(price)
                        .orderValue(tradeVal)
                        .status("EXECUTED")
                        .build();

                // Save passive order first and aggressor order second so buyOrderId >= sellOrderId encodes BUY aggressor
                MarketOrder buyOrder;
                MarketOrder sellOrder;
                if (isBuy) {
                    sellOrder = orderRepository.save(sellOrderBuilder);
                    buyOrder = orderRepository.save(buyOrderBuilder);
                } else {
                    buyOrder = orderRepository.save(buyOrderBuilder);
                    sellOrder = orderRepository.save(sellOrderBuilder);
                }

                MarketTrade trade = MarketTrade.builder()
                        .tradeNo(tradeNo)
                        .company(compOpt.get())
                        .buyOrderId(buyOrder.getOrderId())
                        .sellOrderId(sellOrder.getOrderId())
                        .buyerUser(buyer)
                        .sellerUser(seller)
                        .quantity(qty)
                        .tradePrice(price)
                        .tradeValue(tradeVal)
                        .executedAt(OffsetDateTime.now())
                        .status("EXECUTED")
                        .build();

                tradeRepository.save(trade);
            }
        } catch (Throwable e) {
            log.debug("[MarketBroadcastService] Trade record save skipped: {}", e.getMessage());
        }
    }

    private static final Map<String, String> ALIAS_MAP = Map.of(
            "AHIL", "ATG-H",
            "ATTL", "ATG-T",
            "FVTPL", "ATG-E",
            "SIEA", "ATG-F",
            "GEML", "ATG-L",
            "TEBPL", "ATG-R"
    );

    public List<MarketTickDto> getAllTicks() {
        return new ArrayList<>(currentTicks.values());
    }

    public MarketTickDto getTick(String symbol) {
        if (symbol == null) return null;
        String clean = symbol.trim().toUpperCase();
        MarketTickDto tick = currentTicks.get(clean);
        if (tick == null) {
            String canonical = ALIAS_MAP.get(clean);
            if (canonical != null) {
                tick = currentTicks.get(canonical);
            }
        }
        return tick;
    }

    public OrderBookUpdateDto getOrderBook(String symbol) {
        if (symbol == null) return null;
        String clean = symbol.trim().toUpperCase();
        OrderBookUpdateDto ob = currentOrderBooks.get(clean);
        if (ob == null) {
            String canonical = ALIAS_MAP.get(clean);
            if (canonical != null) {
                ob = currentOrderBooks.get(canonical);
            }
        }
        return ob;
    }
}
