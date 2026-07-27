package com.bestrookies.portfolio.dto;

import java.time.Instant;

public record PortfolioResponse(
    Long id,
    String name,
    String baseCurrency,
    Instant createdAt
) {
}

