package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.entity.AssetType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 实时代码搜索服务：基于 Yahoo Finance Search API 拉取各资产类型的可选代码。
 */
@Service
public class MarketSymbolService {

    private static final Logger log = LoggerFactory.getLogger(MarketSymbolService.class);

    private static final String DEFAULT_SEARCH_ENDPOINT = "https://query1.finance.yahoo.com/v1/finance/search?q={q}";

    private static final Map<AssetType, List<String>> DEFAULT_QUERIES = buildQueries();
    private static final Map<AssetType, Set<String>> ALLOWED_QUOTE_TYPES = buildAllowedQuoteTypes();

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String searchEndpoint;

    public MarketSymbolService(
        ObjectMapper objectMapper,
        @Value("${app.market-symbol.enabled:true}") boolean enabled,
        @Value("${app.market-symbol.search-endpoint:" + DEFAULT_SEARCH_ENDPOINT + "}") String searchEndpoint
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.searchEndpoint = searchEndpoint;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    /**
     * 按资产类型获取实时可选代码列表。
     *
     * @param assetType 资产类型
     * @param limit 最多返回数量
     * @return ticker 列表，API失败时返回空列表
     */
    public List<String> fetchRealtimeTickersByAssetType(AssetType assetType, int limit) {
        if (!enabled || assetType == null || limit <= 0) {
            return List.of();
        }

        List<String> queries = DEFAULT_QUERIES.getOrDefault(assetType, List.of());
        Set<String> allowedQuoteTypes = ALLOWED_QUOTE_TYPES.getOrDefault(assetType, Set.of());
        LinkedHashSet<String> merged = new LinkedHashSet<>();

        for (String q : queries) {
            if (merged.size() >= limit) {
                break;
            }
            List<String> symbols = searchSymbols(q, allowedQuoteTypes, limit - merged.size());
            merged.addAll(symbols);
        }

        return List.copyOf(merged);
    }

    /**
     * 按资产类型 + 关键字搜索实时可选代码。
     *
     * @param assetType 资产类型
     * @param query 用户输入的关键字（如 AAPL、TES、BTC）
     * @param limit 最多返回数量
     * @return ticker 列表
     */
    public List<String> searchRealtimeTickersByAssetType(AssetType assetType, String query, int limit) {
        if (!enabled || assetType == null || query == null || query.isBlank() || limit <= 0) {
            return List.of();
        }

        Set<String> allowedQuoteTypes = ALLOWED_QUOTE_TYPES.getOrDefault(assetType, Set.of());
        return searchSymbols(query.trim(), allowedQuoteTypes, limit);
    }

    private List<String> searchSymbols(String q, Set<String> allowedQuoteTypes, int limit) {
        if (q == null || q.isBlank() || limit <= 0) {
            return List.of();
        }

        String encoded = URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
        String endpoint = searchEndpoint.replace("{q}", encoded);
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(Duration.ofSeconds(4))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return List.of();
            }
            return extractSymbols(response.body(), allowedQuoteTypes, limit);
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("实时代码搜索失败, query={}: {}", q, ex.getMessage());
            return List.of();
        }
    }

    private List<String> extractSymbols(String body, Set<String> allowedQuoteTypes, int limit) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode quotes = root.path("quotes");
        if (!quotes.isArray() || quotes.isEmpty()) {
            return List.of();
        }

        List<String> out = new ArrayList<>();
        for (JsonNode quote : quotes) {
            if (out.size() >= limit) {
                break;
            }
            String symbol = quote.path("symbol").asText("").trim();
            String quoteType = quote.path("quoteType").asText("").trim().toUpperCase();

            if (symbol.isEmpty()) {
                continue;
            }
            if (!allowedQuoteTypes.isEmpty() && !allowedQuoteTypes.contains(quoteType)) {
                continue;
            }
            out.add(symbol.toUpperCase());
        }
        return out;
    }

    private static Map<AssetType, List<String>> buildQueries() {
        Map<AssetType, List<String>> map = new EnumMap<>(AssetType.class);
        map.put(AssetType.STOCK, List.of("AAPL", "MSFT", "TSLA", "AMZN", "NVDA"));
        map.put(AssetType.BOND, List.of("BND", "TLT", "AGG", "IEF"));
        map.put(AssetType.CASH, List.of("USD", "EUR", "JPY", "GBP", "CNY"));
        map.put(AssetType.ETF, List.of("SPY", "QQQ", "VTI", "VOO"));
        map.put(AssetType.FUND, List.of("VFIAX", "SWPPX", "FXAIX"));
        map.put(AssetType.CRYPTO, List.of("BTC", "ETH", "SOL", "XRP"));
        return Map.copyOf(map);
    }

    private static Map<AssetType, Set<String>> buildAllowedQuoteTypes() {
        Map<AssetType, Set<String>> map = new EnumMap<>(AssetType.class);
        map.put(AssetType.STOCK, Set.of("EQUITY"));
        map.put(AssetType.BOND, Set.of("BOND", "ETF"));
        map.put(AssetType.CASH, Set.of("CURRENCY"));
        map.put(AssetType.ETF, Set.of("ETF"));
        map.put(AssetType.FUND, Set.of("MUTUALFUND"));
        map.put(AssetType.CRYPTO, Set.of("CRYPTOCURRENCY"));
        return Map.copyOf(map);
    }
}

