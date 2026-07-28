package com.bestrookies.portfolio.dto;

import java.math.BigDecimal;

/**
 * 投资组合摘要响应 DTO。
 *
 * <p>字段说明：
 * <ul>
 *   <li>totalCost           - 总投入成本（数量 × 均价，换算为 baseCurrency）</li>
 *   <li>marketValue         - 总市值（使用最新行情价格，无行情时回退均价，换算为 baseCurrency）</li>
 *   <li>unrealizedPnL       - 未实现盈亏 = marketValue - totalCost</li>
 *   <li>returnRate          - 收益率百分比 = unrealizedPnL / totalCost × 100，保留 2 位小数</li>
 *   <li>positionsWithLivePrice - 本次汇总中获取到实时行情的持仓数量</li>
 *   <li>positionsWithFallback  - 因无行情而回退均价的持仓数量</li>
 *   <li>baseCurrency        - 所有金额统一换算的目标币种</li>
 * </ul>
 */
public record PortfolioSummaryResponse(
    Long portfolioId,
    int totalPositions,
    BigDecimal totalCost,
    BigDecimal marketValue,
    BigDecimal unrealizedPnL,
    BigDecimal returnRate,
    int positionsWithLivePrice,
    int positionsWithFallback,
    String baseCurrency
) {
}

