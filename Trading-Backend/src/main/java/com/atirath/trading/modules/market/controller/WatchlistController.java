package com.atirath.trading.modules.market.controller;

import com.atirath.trading.common.ApiResponse;
import com.atirath.trading.config.JwtTokenProvider;
import com.atirath.trading.modules.market.dto.WatchlistDto;
import com.atirath.trading.modules.market.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/market/watchlist")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Member Watchlists", description = "Member's watched market companies")
@SecurityRequirement(name = "BearerAuthentication")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    @Operation(summary = "Get Member Watchlist", description = "Returns all companies currently in the authenticated user's watchlist with price metrics")
    public ResponseEntity<ApiResponse<List<WatchlistDto>>> getWatchlist(
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        List<WatchlistDto> watchlist = watchlistService.getUserWatchlist(userId);
        return ResponseEntity.ok(ApiResponse.success(watchlist, "Watchlist retrieved successfully"));
    }

    @PostMapping("/{companyId}")
    @Operation(summary = "Add Company to Watchlist", description = "Adds a company to the authenticated user's watchlist")
    public ResponseEntity<ApiResponse<Void>> addToWatchlist(
            @PathVariable("companyId") Long companyId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        watchlistService.addToWatchlist(userId, companyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Company added to watchlist successfully"));
    }

    @DeleteMapping("/{companyId}")
    @Operation(summary = "Remove Company from Watchlist", description = "Removes a company from the authenticated user's watchlist")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @PathVariable("companyId") Long companyId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        watchlistService.removeFromWatchlist(userId, companyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Company removed from watchlist successfully"));
    }

    @GetMapping("/{companyId}/status")
    @Operation(summary = "Check Watchlist Status", description = "Checks whether a company is currently watched by the authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStatus(
            @PathVariable("companyId") Long companyId,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        boolean watched = watchlistService.isWatched(userId, companyId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("isWatchlisted", watched)));
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
