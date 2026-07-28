package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.dto.PriceSnapshotResponse;
import com.bestrookies.portfolio.entity.PriceSnapshot;
import com.bestrookies.portfolio.exception.ResourceNotFoundException;
import com.bestrookies.portfolio.repository.PriceSnapshotRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceSnapshotService {

    private final PriceSnapshotRepository priceSnapshotRepository;

    public PriceSnapshotService(PriceSnapshotRepository priceSnapshotRepository) {
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    /**
     * Persist a new price snapshot (called by external price-fetch logic or a scheduled job).
     */
    @Transactional
    public PriceSnapshotResponse saveSnapshot(String ticker, BigDecimal price, String currency) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setTicker(ticker.toUpperCase().trim());
        snapshot.setPrice(price);
        snapshot.setCurrency(currency.toUpperCase());
        snapshot.setFetchedAt(Instant.now());
        return toResponse(priceSnapshotRepository.save(snapshot));
    }

    /**
     * Return the latest known price for the given ticker.
     */
    @Transactional(readOnly = true)
    public PriceSnapshotResponse getLatestPrice(String ticker) {
        PriceSnapshot snapshot = findLatestSnapshot(ticker)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No price snapshot found for ticker: " + ticker));
        return toResponse(snapshot);
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> findLatestPriceValue(String ticker) {
        return findLatestSnapshot(ticker).map(PriceSnapshot::getPrice);
    }

    @Transactional(readOnly = true)
    public Optional<PriceSnapshot> findLatestSnapshot(String ticker) {
        return priceSnapshotRepository.findTopByTickerOrderByFetchedAtDesc(ticker.toUpperCase().trim());
    }

    private PriceSnapshotResponse toResponse(PriceSnapshot s) {
        return new PriceSnapshotResponse(s.getId(), s.getTicker(), s.getPrice(), s.getCurrency(), s.getFetchedAt());
    }
}
