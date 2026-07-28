package com.bestrookies.portfolio.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 汇率换算服务（固定汇率，不依赖外部 API）。
 *
 * <p>所有币种先换算为 USD，再按需要从 USD 换算到目标币种。
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private static final String USD = "USD";

    /**
     * 固定汇率表：1 单位币种 = ? USD。
     * 汇率为静态值，仅用于简化本地估值。
     */
    private static final Map<String, BigDecimal> TO_USD_RATE = Map.of(
        USD, BigDecimal.ONE,
        "EUR", new BigDecimal("1.08"),
        "GBP", new BigDecimal("1.27"),
        "JPY", new BigDecimal("0.0067"),
        "CNY", new BigDecimal("0.14"),
        "HKD", new BigDecimal("0.128"),
        "CAD", new BigDecimal("0.73"),
        "AUD", new BigDecimal("0.66")
    );

    /**
     * 获取从 fromCurrency 到 toCurrency 的汇率。
     *
     * @param fromCurrency 源币种，例如 EUR
     * @param toCurrency   目标币种，例如 USD
     * @return 汇率（例如 1.08 表示 1 EUR = 1.08 USD），未知币种时回退 1.0
     */
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency == null ? "" : fromCurrency.trim().toUpperCase();
        String to = toCurrency == null ? "" : toCurrency.trim().toUpperCase();

        if (from.isEmpty() || to.isEmpty() || from.equals(to)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromToUsd = TO_USD_RATE.get(from);
        BigDecimal toToUsd = TO_USD_RATE.get(to);
        if (fromToUsd == null || toToUsd == null) {
            log.warn("未知币种汇率 {}/{}，回退使用 1.0", from, to);
            return BigDecimal.ONE;
        }

        // A -> B = (A -> USD) / (B -> USD)
        return fromToUsd.divide(toToUsd, 8, RoundingMode.HALF_UP);
    }
}

