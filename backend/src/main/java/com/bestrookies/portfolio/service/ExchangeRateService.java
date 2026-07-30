package com.bestrookies.portfolio.service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.fx.FxQuote;

/**
 * 汇率换算服务。
 *
 * <p>启动时及每小时通过 Yahoo Finance API 拉取最新外汇汇率（以 USD 为基准，无需 API Key）。
 * 若拉取失败则回退到内置默认汇率，保证业务不中断。
 *
 * <p>汇率格式：perUsdRates["EUR"] = 0.92 表示 1 USD = 0.92 EUR。
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    /** 内置默认汇率（1 USD = X 目标币种），仅在 Yahoo Finance 不可用时使用 */
    private static final Map<String, BigDecimal> DEFAULT_RATES = Map.of(
        "USD", BigDecimal.ONE,
        "EUR", new BigDecimal("0.92"),
        "GBP", new BigDecimal("0.79"),
        "CNY", new BigDecimal("7.25"),
        "HKD", new BigDecimal("7.80"),
        "JPY", new BigDecimal("149.50"),
        "CAD", new BigDecimal("1.37"),
        "AUD", new BigDecimal("1.52")
    );

    /**
     * 需要拉取的外汇代码列表，格式 USD{目标}=X，表示 1 USD 换多少目标币种。
     * 例如 "USDEUR=X" → price = 0.92 → 1 USD = 0.92 EUR。
     */
    private static final List<String> FX_SYMBOLS = List.of(
        "USDEUR=X", "USDGBP=X", "USDCNY=X",
        "USDHKD=X", "USDJPY=X", "USDCAD=X", "USDAUD=X"
    );

    @Value("${app.exchange-rate.enabled:true}")
    private boolean enabled;

    /**
     * 当前生效的汇率表（1 USD = X 目标币种），volatile 保证多线程可见性。
     * 初始值为内置默认汇率，Yahoo Finance 成功后会替换为最新值。
     */
    private volatile Map<String, BigDecimal> perUsdRates = DEFAULT_RATES;

    /** 启动时立即拉取一次汇率 */
    @PostConstruct
    public void init() {
        refresh();
    }

    /**
     * 定时刷新汇率，默认每小时执行一次。
     * 间隔可通过 app.exchange-rate.refresh-interval-ms 配置。
     */
    @Scheduled(fixedRateString = "${app.exchange-rate.refresh-interval-ms:3600000}")
    public void refresh() {
        if (!enabled) {
            log.info("汇率刷新已禁用，使用内置默认汇率");
            return;
        }
        try {
            Map<String, BigDecimal> updated = new HashMap<>();
            updated.put("USD", BigDecimal.ONE);

            for (String symbol : FX_SYMBOLS) {
                try {
                    FxQuote quote = YahooFinance.getFx(symbol);
                    if (quote != null && quote.getPrice() != null) {
                        // "USDEUR=X" → 截取第 3-6 位得到目标币种 "EUR"
                        String currency = symbol.substring(3, 6);
                        updated.put(currency, quote.getPrice());
                        log.debug("  {} → 1 USD = {} {}", symbol, quote.getPrice(), currency);
                    }
                } catch (Exception e) {
                    log.warn("获取 {} 汇率失败，将使用默认值: {}", symbol, e.getMessage());
                }
            }

            if (updated.size() > 1) {
                perUsdRates = Map.copyOf(updated);
                log.info("Yahoo Finance 汇率刷新成功，共 {} 种货币", perUsdRates.size());
            } else {
                log.warn("Yahoo Finance 未返回有效汇率，继续使用当前汇率");
            }
        } catch (Exception e) {
            log.error("Yahoo Finance 汇率刷新失败，继续使用当前汇率: {}", e.getMessage());
        }
    }

    /**
     * 获取从 fromCurrency 到 toCurrency 的换算汇率。
     *
     * <p>计算方式：perUsdRates 存储 "1 USD = X 目标"，
     * 因此 from → to = perUsdRates[to] / perUsdRates[from]。
     *
     * @param fromCurrency 源币种，例如 EUR
     * @param toCurrency   目标币种，例如 USD
     * @return 汇率，例如 1.087 表示 1 EUR = 1.087 USD；未知币种回退 1.0
     */
    public BigDecimal getRate(String fromCurrency, String toCurrency) {
        String from = fromCurrency == null ? "" : fromCurrency.trim().toUpperCase();
        String to   = toCurrency   == null ? "" : toCurrency.trim().toUpperCase();

        if (from.isEmpty() || to.isEmpty() || from.equals(to)) {
            return BigDecimal.ONE;
        }

        BigDecimal fromPerUsd = perUsdRates.get(from);
        BigDecimal toPerUsd   = perUsdRates.get(to);
        if (fromPerUsd == null || toPerUsd == null) {
            log.warn("汇率表中未找到 {}/{} 的汇率，回退使用 1.0", from, to);
            return BigDecimal.ONE;
        }

        // from → to = perUsdRates[to] / perUsdRates[from]
        return toPerUsd.divide(fromPerUsd, 8, RoundingMode.HALF_UP);
    }
}
