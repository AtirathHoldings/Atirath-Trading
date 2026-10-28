package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.modules.market.dto.MarketTickDto;
import com.atirath.trading.modules.market.dto.OrderBookUpdateDto;
import com.atirath.trading.modules.market.service.MarketBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(name = "Real-Time WebSocket & Market Depth", description = "Live STOMP mappings and instant snapshots for streaming quotes")
public class MarketWebSocketController {

    private final MarketBroadcastService broadcastService;

    // =========================================================================
    // STOMP Message Mappings
    // =========================================================================

    @MessageMapping("/market/snapshot")
    @SendTo("/topic/market/snapshot")
    public List<MarketTickDto> getMarketSnapshot() {
        return broadcastService.getAllTicks();
    }

    @MessageMapping("/orderbook/{symbol}")
    @SendTo("/topic/orderbook/{symbol}")
    public OrderBookUpdateDto getOrderBookSnapshot(@DestinationVariable String symbol) {
        return broadcastService.getOrderBook(symbol);
    }

    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public Map<String, Object> ping() {
        return Map.of("type", "PONG", "timestamp", System.currentTimeMillis());
    }

    // =========================================================================
    // REST Snapshots (Fallback and instant initial state for screens)
    // =========================================================================

    @GetMapping("/api/v1/market/live/snapshot")
    @ResponseBody
    @Operation(summary = "Get Live Market Ticks Snapshot", description = "Returns active live streaming market ticks for instant screen loading")
    public ResponseEntity<ApiResponse<List<MarketTickDto>>> getLiveTicksSnapshot() {
        return ResponseEntity.ok(ApiResponse.success(broadcastService.getAllTicks(), "Live ticks retrieved"));
    }

    @GetMapping("/api/v1/market/live/orderbook/{symbol}")
    @ResponseBody
    @Operation(summary = "Get Live Order Book Snapshot", description = "Returns active 5-level market depth order book for a symbol")
    public ResponseEntity<ApiResponse<OrderBookUpdateDto>> getLiveOrderBook(@PathVariable String symbol) {
        OrderBookUpdateDto ob = broadcastService.getOrderBook(symbol);
        return ResponseEntity.ok(ApiResponse.success(ob, "Order book retrieved"));
    }
}
