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
import java.util.Optional;
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
    private final ExchangeRateService exchangeRateService;

    public PortfolioService(
        PortfolioRepository portfolioRepository,
        PositionRepository positionRepository,
        MarketPriceService marketPriceService,
        PriceSnapshotService priceSnapshotService,
        ExchangeRateService exchangeRateService
    ) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.marketPriceService = marketPriceService;
        this.priceSnapshotService = priceSnapshotService;
        this.exchangeRateService = exchangeRateService;
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

    /**
     * 计算组合摘要，包含完整收益计算：
     * <ul>
     *   <li>totalCost      - 所有持仓成本之和（换算为 baseCurrency）</li>
     *   <li>marketValue    - 所有持仓市值之和（优先用实时行情，无行情回退均价，换算为 baseCurrency）</li>
     *   <li>unrealizedPnL  - marketValue - totalCost</li>
     *   <li>returnRate     - unrealizedPnL / totalCost × 100（%）</li>
     *   <li>positionsWithLivePrice - 本次获取到行情的持仓数</li>
     *   <li>positionsWithFallback  - 回退均价的持仓数</li>
     * </ul>
     *
     * <p>汇率规则：若持仓币种与组合 baseCurrency 不同，通过 ExchangeRateService
     * 转换（无法获取汇率时回退 1.0，并在日志中记录警告）。
     */
    @Transactional
    public PortfolioSummaryResponse getSummary(Long portfolioId) {
        Portfolio portfolio = getPortfolioEntity(portfolioId);
        String baseCurrency = portfolio.getBaseCurrency();

        List<Position> positions = positionRepository
            .findByPortfolioId(portfolio.getId(), Pageable.unpaged())
            .getContent();

        // 逐个持仓估值并聚合
        List<PositionValuation> valuations = positions.stream()
            .map(p -> valuatePosition(p, baseCurrency))
            .toList();

        // 总投入成本（baseCurrency）
        BigDecimal totalCost = valuations.stream()
            .map(PositionValuation::costInBase)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        // 总市值（baseCurrency）
        BigDecimal marketValue = valuations.stream()
            .map(PositionValuation::marketValueInBase)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_UP);

        // 未实现盈亏
        BigDecimal unrealizedPnL = marketValue.subtract(totalCost)
            .setScale(4, RoundingMode.HALF_UP);

        // 收益率（%），零成本时返回 0 避免除零异常
        BigDecimal returnRate = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) != 0) {
            returnRate = unrealizedPnL
                .divide(totalCost, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        }

        // 数据质量：统计有实时行情与回退均价的持仓数
        int positionsWithLivePrice = (int) valuations.stream()
            .filter(PositionValuation::hasLivePrice)
            .count();
        int positionsWithFallback = positions.size() - positionsWithLivePrice;

        return new PortfolioSummaryResponse(
            portfolio.getId(),
            positions.size(),
            totalCost,
            marketValue,
            unrealizedPnL,
            returnRate,
            positionsWithLivePrice,
            positionsWithFallback,
            baseCurrency
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

    // ---------------------------------------------------------------------------
    // 私有辅助方法
    // ---------------------------------------------------------------------------

    /**
     * 对单个持仓进行估值。
     *
     * @param position     持仓实体
     * @param baseCurrency 组合基础币种
     * @return 估值结果（成本、市值、是否有实时行情）
     */
    private PositionValuation valuatePosition(Position position, String baseCurrency) {
        // 汇率：持仓币种 -> 组合基础币种
        BigDecimal fxRate = exchangeRateService.getRate(position.getCurrency(), baseCurrency);

        // 持仓成本（baseCurrency）= 数量 × 均价 × 汇率
        BigDecimal costInBase = position.getQuantity()
            .multiply(position.getAvgCost())
            .multiply(fxRate)
            .setScale(4, RoundingMode.HALF_UP);

        // 尝试获取实时行情
        Optional<BigDecimal> livePrice = marketPriceService.fetchLatestPrice(position.getTicker());
        boolean hasLive = livePrice.isPresent();

        if (hasLive) {
            // 有实时行情时持久化快照，用于后续历史趋势查询
            priceSnapshotService.saveSnapshot(position.getTicker(), livePrice.get(), position.getCurrency());
        }

        // 当前价格优先级：实时行情 > 历史快照 > 持仓均价（最终回退）
        BigDecimal currentPrice = livePrice
            .or(() -> priceSnapshotService.findLatestPriceValue(position.getTicker()))
            .orElse(position.getAvgCost());

        // 持仓市值（baseCurrency）= 数量 × 当前价格 × 汇率
        BigDecimal marketValueInBase = position.getQuantity()
            .multiply(currentPrice)
            .multiply(fxRate)
            .setScale(4, RoundingMode.HALF_UP);

        return new PositionValuation(costInBase, marketValueInBase, hasLive);
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
            portfolio.getId(),
            portfolio.getName(),
            portfolio.getBaseCurrency(),
            portfolio.getCreatedAt()
        );
    }

    // ---------------------------------------------------------------------------
    // 内部估值记录（仅用于 getSummary 内部聚合）
    // ---------------------------------------------------------------------------

    /**
     * 单个持仓的估值结果。
     *
     * @param costInBase        该持仓的投入成本（已换算为 baseCurrency）
     * @param marketValueInBase 该持仓的当前市值（已换算为 baseCurrency）
     * @param hasLivePrice      本次是否获取到实时行情
     */
    private record PositionValuation(
        BigDecimal costInBase,
        BigDecimal marketValueInBase,
        boolean hasLivePrice
    ) {}
}
