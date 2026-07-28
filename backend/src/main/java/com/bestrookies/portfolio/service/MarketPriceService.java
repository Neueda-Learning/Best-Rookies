package com.bestrookies.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MarketPriceService {

    private static final String DEFAULT_API =
        "https://c4rm9elh30.execute-api.us-east-1.amazonaws.com/default/cachedPriceData?ticker={ticker}";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String endpointTemplate;

    public MarketPriceService(
        ObjectMapper objectMapper,
        @Value("${app.market-price.enabled:true}") boolean enabled,
        @Value("${app.market-price.endpoint:" + DEFAULT_API + "}") String endpointTemplate
    ) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.endpointTemplate = endpointTemplate;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    }

    public Optional<BigDecimal> fetchLatestPrice(String ticker) {
        if (!enabled || ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }

        String encodedTicker = URLEncoder.encode(ticker.trim().toUpperCase(), StandardCharsets.UTF_8);
        String endpoint = endpointTemplate.replace("{ticker}", encodedTicker);

        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(Duration.ofSeconds(4))
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            return extractLastClosePrice(response.body());
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> extractLastClosePrice(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        JsonNode closes = root.path("price_data").path("close");

        if (!closes.isArray() || closes.isEmpty()) {
            return Optional.empty();
        }

        JsonNode latestNode = closes.get(closes.size() - 1);
        if (!latestNode.isNumber()) {
            return Optional.empty();
        }

        BigDecimal latest = latestNode.decimalValue().setScale(4, RoundingMode.HALF_UP);
        return Optional.of(latest);
    }
}

