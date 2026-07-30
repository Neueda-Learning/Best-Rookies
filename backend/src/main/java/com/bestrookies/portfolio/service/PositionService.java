package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.dto.PositionCreateRequest;
import com.bestrookies.portfolio.dto.PositionResponse;
import com.bestrookies.portfolio.dto.PositionUpdateRequest;
import com.bestrookies.portfolio.entity.AssetType;
import com.bestrookies.portfolio.entity.Portfolio;
import com.bestrookies.portfolio.entity.Position;
import com.bestrookies.portfolio.exception.ResourceNotFoundException;
import com.bestrookies.portfolio.repository.PositionRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    /**
     * 预置代码池：用于前端按资产类型下拉选择“常用代码”。
     * STOCK 先对齐当前行情API支持代码，其他类型给出常见示例。
     */
    private static final Map<AssetType, List<String>> PRESET_TICKERS = buildPresetTickers();

    private final PositionRepository positionRepository;
    private final PortfolioService portfolioService;
    private final MarketSymbolService marketSymbolService;

    public PositionService(
        PositionRepository positionRepository,
        PortfolioService portfolioService,
        MarketSymbolService marketSymbolService
    ) {
        this.positionRepository = positionRepository;
        this.portfolioService = portfolioService;
        this.marketSymbolService = marketSymbolService;
    }

    @Transactional
    public PositionResponse createPosition(PositionCreateRequest request) {
        Portfolio portfolio = portfolioService.getPortfolioEntity(request.portfolioId());
        Instant effectiveUpdatedAt = request.updatedAt() != null ? request.updatedAt() : Instant.now();

        Position position = new Position();
        position.setPortfolio(portfolio);
        position.setAssetType(request.assetType());
        position.setTicker(request.ticker().toUpperCase().trim());
        position.setQuantity(request.quantity());
        position.setAvgCost(request.avgCost());
        position.setCurrency(request.currency().trim().toUpperCase());
        // 新增持仓时允许前端显式指定日期，用于回填历史录入场景；未传时仍回退当前时间。
        position.setUpdatedAt(effectiveUpdatedAt);

        return toResponse(positionRepository.save(position));
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> listPositions(Long portfolioId, Pageable pageable) {
        Page<Position> positions = portfolioId == null
            ? positionRepository.findAll(pageable)
            : positionRepository.findByPortfolioId(portfolioId, pageable);

        return positions.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<String> listAssetTypes() {
        return Arrays.stream(AssetType.values()).map(Enum::name).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listSupportedTickers(AssetType assetType) {
        List<String> realtime = marketSymbolService.fetchRealtimeTickersByAssetType(assetType, 20);
        List<String> preset = PRESET_TICKERS.getOrDefault(assetType, List.of());
        List<String> existing = positionRepository.findDistinctTickersByAssetType(assetType);

        // 返回顺序：实时结果 -> 预置常用 -> 数据库已有，避免下拉项重复。
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(realtime);
        merged.addAll(preset);
        merged.addAll(existing);
        return List.copyOf(merged);
    }

    @Transactional(readOnly = true)
    public List<String> searchSupportedTickers(AssetType assetType, String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        String normalized = query == null ? "" : query.trim().toUpperCase(Locale.ROOT);
        if (assetType == null || normalized.isBlank()) {
            return List.of();
        }

        List<String> realtime = marketSymbolService.searchRealtimeTickersByAssetType(assetType, normalized, safeLimit);
        List<String> preset = PRESET_TICKERS.getOrDefault(assetType, List.of());
        List<String> existing = positionRepository.findDistinctTickersByAssetType(assetType);

        LinkedHashSet<String> merged = new LinkedHashSet<>();
        merged.addAll(realtime);
        preset.stream().filter(t -> t.contains(normalized)).forEach(merged::add);
        existing.stream()
            .map(t -> t == null ? "" : t.trim().toUpperCase(Locale.ROOT))
            .filter(t -> !t.isEmpty() && t.contains(normalized))
            .forEach(merged::add);

        return merged.stream().limit(safeLimit).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<String>> listSupportedTickersForAllAssetTypes() {
        Map<String, List<String>> out = new java.util.LinkedHashMap<>();
        for (AssetType type : AssetType.values()) {
            out.put(type.name(), listSupportedTickers(type));
        }
        return out;
    }

    @Transactional
    public PositionResponse updatePosition(Long id, PositionUpdateRequest request) {
        Position position = getPositionEntity(id);

        // PATCH 语义：仅更新调用方提供的字段
        if (request.quantity() != null) {
            position.setQuantity(request.quantity());
        }
        if (request.avgCost() != null) {
            position.setAvgCost(request.avgCost());
        }
        position.setUpdatedAt(Instant.now());

        return toResponse(positionRepository.save(position));
    }

    @Transactional
    public void deletePosition(Long id) {
        Position position = getPositionEntity(id);
        positionRepository.delete(position);
    }

    @Transactional(readOnly = true)
    public Position getPositionEntity(Long id) {
        return positionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("持仓不存在：" + id));
    }

    private PositionResponse toResponse(Position position) {
        return new PositionResponse(
            position.getId(),
            position.getPortfolio().getId(),
            position.getAssetType(),
            position.getTicker(),
            position.getQuantity(),
            position.getAvgCost(),
            position.getCurrency(),
            position.getUpdatedAt()
        );
    }

    private static Map<AssetType, List<String>> buildPresetTickers() {
        Map<AssetType, List<String>> map = new EnumMap<>(AssetType.class);
        map.put(AssetType.STOCK, List.of("AAPL", "AMZN", "TSLA", "C", "FB", "SAP"));
        map.put(AssetType.BOND, List.of("BND", "TLT", "IEF", "AGG"));
        map.put(AssetType.CASH, List.of("USD", "EUR", "CNY", "GBP", "HKD"));
        map.put(AssetType.ETF, List.of("SPY", "QQQ", "VTI", "VOO"));
        map.put(AssetType.FUND, List.of("VFIAX", "SWPPX", "FXAIX"));
        map.put(AssetType.CRYPTO, List.of("BTC-USD", "ETH-USD", "SOL-USD"));
        return Map.copyOf(map);
    }
}

