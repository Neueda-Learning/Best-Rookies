package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.dto.PortfolioCreateRequest;
import com.bestrookies.portfolio.dto.PortfolioResponse;
import com.bestrookies.portfolio.dto.PortfolioSummaryResponse;
import com.bestrookies.portfolio.dto.PortfolioUpdateRequest;
import com.bestrookies.portfolio.entity.Portfolio;
import com.bestrookies.portfolio.entity.Position;
import com.bestrookies.portfolio.exception.ResourceNotFoundException;
import com.bestrookies.portfolio.repository.PortfolioRepository;
import com.bestrookies.portfolio.repository.PositionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final MarketPriceService marketPriceService;
    private final PriceSnapshotService priceSnapshotService;

    public PortfolioService(
        PortfolioRepository portfolioRepository,
        PositionRepository positionRepository,
        MarketPriceService marketPriceService,
        PriceSnapshotService priceSnapshotService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.marketPriceService = marketPriceService;
        this.priceSnapshotService = priceSnapshotService;
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreateRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency().trim().toUpperCase());
        portfolio.setCreatedAt(Instant.now());
        Portfolio saved = portfolioRepository.save(portfolio);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PortfolioResponse> listPortfolios(Pageable pageable) {
        return portfolioRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long portfolioId) {
        return toResponse(getPortfolioEntity(portfolioId));
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId(), Pageable.unpaged()).getContent();

        BigDecimal totalCost = positions.stream()
            .map(position -> position.getQuantity().multiply(position.getAvgCost()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        BigDecimal marketValue = positions.stream()
            .map(this::calculatePositionMarketValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        BigDecimal unrealizedPnL = marketValue.subtract(totalCost).setScale(4, RoundingMode.HALF_UP);

        return new PortfolioSummaryResponse(
            portfolio.getId(),
            positions.size(),
            totalCost,
            marketValue,
            unrealizedPnL
        );
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioUpdateRequest request) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        if (request.name() != null && !request.name().isBlank()) {
            portfolio.setName(request.name().trim());
        }
        if (request.baseCurrency() != null && !request.baseCurrency().isBlank()) {
            portfolio.setBaseCurrency(request.baseCurrency().trim().toUpperCase());
        }
        return toResponse(portfolioRepository.save(portfolio));
    }

    @Transactional
    public void deletePortfolio(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        positionRepository.deleteByPortfolioId(portfolioId);
        portfolioRepository.delete(portfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioEntity(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
    }

    private BigDecimal calculatePositionMarketValue(Position position) {
        BigDecimal fallbackPrice = position.getAvgCost();
        BigDecimal currentPrice = marketPriceService.fetchLatestPrice(position.getTicker())
            .map(price -> {
                priceSnapshotService.saveSnapshot(position.getTicker(), price, position.getCurrency());
                return price;
            })
            .or(() -> priceSnapshotService.findLatestPriceValue(position.getTicker()))
            .orElse(fallbackPrice);

        return position.getQuantity().multiply(currentPrice);
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency(),
            portfolio.getCreatedAt()
        );
    }
}
