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
        // 摘要从已保存的持仓计算得出，保持逻辑集中
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

    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, PortfolioUpdateRequest request) {
        // 获取要更新的组合，如果不存在则抛出异常
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 更新组合信息
        portfolio.setName(request.name().trim());
        portfolio.setBaseCurrency(request.baseCurrency().toUpperCase());

        // 保存更新后的组合
        Portfolio updated = portfolioRepository.save(portfolio);
        return toResponse(updated);
    }

    @Transactional
    public void deletePortfolio(Long portfolioId) {
        // 获取要删除的组合，如果不存在则抛出异常
        Portfolio portfolio = getPortfolioEntity(portfolioId);

        // 删除组合（级联删除所有关联的持仓，由 JPA 的 orphanRemoval=true 自动处理）
        portfolioRepository.delete(portfolio);
    }
}
