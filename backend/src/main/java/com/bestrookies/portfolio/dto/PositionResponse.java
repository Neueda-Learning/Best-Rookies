package com.bestrookies.portfolio.dto;

import com.bestrookies.portfolio.entity.AssetType;
import java.math.BigDecimal;
import java.time.Instant;

public record PositionResponse(
    Long id,
    Long portfolioId,
    AssetType assetType,
    String ticker,
    BigDecimal quantity,
    BigDecimal avgCost,
    String currency,
    Instant updatedAt
) {
}

