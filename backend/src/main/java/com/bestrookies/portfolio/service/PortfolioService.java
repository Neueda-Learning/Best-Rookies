package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.dto.PortfolioCreateRequest;
import com.bestrookies.portfolio.dto.PortfolioResponse;
import com.bestrookies.portfolio.dto.PortfolioSummaryResponse;
import com.bestrookies.portfolio.entity.Portfolio;
import com.bestrookies.portfolio.entity.Position;
import com.bestrookies.portfolio.exception.ResourceNotFoundException;
import com.bestrookies.portfolio.repository.PortfolioRepository;
import com.bestrookies.portfolio.repository.PositionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, PositionRepository positionRepository) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
    }

    @Transactional
    public PortfolioResponse createPortfolio(PortfolioCreateRequest request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency().toUpperCase());
        portfolio.setCreatedAt(Instant.now());
        Portfolio saved = portfolioRepository.save(portfolio);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> listPortfolios() {
        return portfolioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(Long portfolioId) {
        return toResponse(getPortfolioEntity(portfolioId));
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long portfolioId) {
        // Summary is computed from persisted positions to keep logic centralized.
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        List<Position> positions = positionRepository.findByPortfolioIdOrderByIdAsc(portfolio.getId());

        BigDecimal totalCost = positions.stream()
            .map(p -> p.getQuantity().multiply(p.getAvgCost()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        return new PortfolioSummaryResponse(portfolio.getId(), positions.size(), totalCost);
    }

    @Transactional(readOnly = true)
    public Portfolio getPortfolioEntity(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
            .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found: " + portfolioId));
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

