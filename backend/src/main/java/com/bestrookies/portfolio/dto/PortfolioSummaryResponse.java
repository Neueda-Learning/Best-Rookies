package com.bestrookies.portfolio.dto;

import java.math.BigDecimal;

public record PortfolioSummaryResponse(
    Long portfolioId,
    int totalPositions,
    BigDecimal totalCost,
    BigDecimal marketValue,
    BigDecimal unrealizedPnL
) {
}
