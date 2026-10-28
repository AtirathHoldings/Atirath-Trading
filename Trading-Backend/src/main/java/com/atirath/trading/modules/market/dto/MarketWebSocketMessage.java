package com.atirath.trading.modules.market.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketWebSocketMessage {
    private String type; // MARKET_TICK, ORDER_BOOK, TRADE, SNAPSHOT, PONG, CONNECTED
    private Object data;
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public static MarketWebSocketMessage of(String type, Object data) {
        return MarketWebSocketMessage.builder()
                .type(type)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
