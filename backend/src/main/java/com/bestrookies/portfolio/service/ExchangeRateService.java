package com.bestrookies.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 汇率换算服务。
 *
 * <p>工作原理：
 * <ol>
 *   <li>若源币种与目标币种相同，直接返回 1.0。</li>
 *   <li>使用内置固定汇率（以 USD 为基准）进行换算。</li>
 *   <li>若币种不在支持列表中，回退 1.0 并打印警告日志。</li>
 * </ol>
 *
 * <p>当前固定基准：1 USD = 0.88 EUR = 6.77 CNY = 0.75 GBP = 7.84 HKD。
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    /** 以 USD 为基准的固定汇率：1 USD = x CCY */
    private static final Map<String, BigDecimal> USD_BASED_RATES = Map.of(
            "USD", BigDecimal.ONE,
            "EUR", new BigDecimal("0.88"),
            "CNY", new BigDecimal("6.77"),
            "GBP", new BigDecimal("0.75"),
            "HKD", new BigDecimal("7.84")
    );

    /**
     * 获取从 fromCurrency 到 toCurrency 的汇率。
     *
     * @param fromCurrency 源币种，例如 EUR
     * @param toCurrency   目标币种，例如 USD
     * @return 汇率（例如 1.08 表示 1 EUR = 1.08 USD），无法获取时返回 1.0
     */
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        // 安全处理空值
        String from = fromCurrency == null ? "" : fromCurrency.trim().toUpperCase();
        String to   = toCurrency   == null ? "" : toCurrency.trim().toUpperCase();

        // 同币种或任意一方为空时，汇率为 1
        if (from.isEmpty() || to.isEmpty() || from.equals(to)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromPerUsd = USD_BASED_RATES.get(from);
        BigDecimal toPerUsd = USD_BASED_RATES.get(to);
        if (fromPerUsd == null || toPerUsd == null) {
            log.warn("不支持的币种换算: {} -> {}，回退使用 1.0", from, to);
            return BigDecimal.ONE;
        }

        // from->to 汇率 = (to/USD) / (from/USD)
        return toPerUsd.divide(fromPerUsd, 8, RoundingMode.HALF_UP);
    }
}

