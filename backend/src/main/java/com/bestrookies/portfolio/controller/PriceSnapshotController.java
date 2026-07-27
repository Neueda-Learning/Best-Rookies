package com.bestrookies.portfolio.controller;

import com.bestrookies.portfolio.dto.PriceSnapshotResponse;
import com.bestrookies.portfolio.service.PriceSnapshotService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceSnapshotController {

    private final PriceSnapshotService priceSnapshotService;

    public PriceSnapshotController(PriceSnapshotService priceSnapshotService) {
        this.priceSnapshotService = priceSnapshotService;
    }

    /**
     * GET /api/v1/prices/{ticker}/latest
     * Returns the most recent stored price snapshot for the given ticker.
     */
    @GetMapping("/{ticker}/latest")
    public PriceSnapshotResponse getLatestPrice(@PathVariable String ticker) {
        return priceSnapshotService.getLatestPrice(ticker);
    }
}

