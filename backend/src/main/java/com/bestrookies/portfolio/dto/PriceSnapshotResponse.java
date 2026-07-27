package com.bestrookies.portfolio.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceSnapshotResponse(
    Long id,
    String ticker,
    BigDecimal price,
    String currency,
    Instant fetchedAt
) {
}

