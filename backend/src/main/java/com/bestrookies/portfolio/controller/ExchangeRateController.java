package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.service.ExchangeRateService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchange-rate")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    /**
     * 示例：GET /api/v1/exchange-rate/rates?baseCurrency=USD&fromCurrencies=EUR,CNY,HKD
     */
    @GetMapping("/rates")
    public ResponseEntity<Map<String, BigDecimal>> getRatesToBase(
        @RequestParam String baseCurrency,
        @RequestParam List<String> fromCurrencies
    ) {
        return ResponseEntity.ok(exchangeRateService.getRatesToBase(fromCurrencies, baseCurrency));
    }
}

