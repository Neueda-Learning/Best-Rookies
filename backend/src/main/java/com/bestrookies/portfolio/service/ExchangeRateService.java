package com.bestrookies.portfolio.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 汇率换算服务。
 *
 * <p>工作原理：
 * <ol>
 *   <li>若源币种与目标币种相同，直接返回 1.0。</li>
 *   <li>尝试通过外部行情 API 获取外汇汇率（格式：{FROM}{TO}=X，例如 EURUSD=X）。</li>
 *   <li>若获取成功，缓存 30 分钟以减少外部请求次数。</li>
 *   <li>若获取失败（API 不支持或外部调用被禁用），回退 1.0 并打印警告日志。</li>
 * </ol>
 *
 * <p>注意：样例 API 主要提供股票行情，外汇汇率通常不可用，大多数情况下会回退 1.0。
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    /** 汇率缓存有效时长：30 分钟 */
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    /** 内存缓存，键格式：FROM_TO，例如 EUR_USD */
    private final ConcurrentHashMap<String, CachedRate> rateCache = new ConcurrentHashMap<>();

    private final MarketPriceService marketPriceService;

    public ExchangeRateService(MarketPriceService marketPriceService) {
        this.marketPriceService = marketPriceService;
    }

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

        String cacheKey = from + "_" + to;

        // 优先使用缓存
        CachedRate cached = rateCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.rate();
        }

        // 尝试通过外部 API 获取汇率（格式：EURUSD=X）
        String forexTicker = from + to + "=X";
        Optional<BigDecimal> fetched = marketPriceService.fetchLatestPrice(forexTicker);

        if (fetched.isPresent()) {
            BigDecimal rate = fetched.get();
            rateCache.put(cacheKey, new CachedRate(rate, Instant.now()));
            log.debug("汇率已更新: {} -> {} = {}", from, to, rate);
            return rate;
        }

        // 回退：返回 1.0 并记录警告
        log.warn("无法获取汇率 {}/{}, 回退使用 1.0（持仓将按原始币种计算）", from, to);
        return BigDecimal.ONE;
    }

    // ---------------------------------------------------------------------------
    // 内部缓存记录
    // ---------------------------------------------------------------------------

    private record CachedRate(BigDecimal rate, Instant fetchedAt) {
        /** 判断缓存是否过期 */
        boolean isExpired() {
            return Instant.now().isAfter(fetchedAt.plus(CACHE_TTL));
        }
    }
}

