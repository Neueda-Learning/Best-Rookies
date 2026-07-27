package com.bestrookies.portfolio.service;

import com.bestrookies.portfolio.dto.PositionCreateRequest;
import com.bestrookies.portfolio.dto.PositionResponse;
import com.bestrookies.portfolio.dto.PositionUpdateRequest;
import com.bestrookies.portfolio.entity.Portfolio;
import com.bestrookies.portfolio.entity.Position;
import com.bestrookies.portfolio.exception.ResourceNotFoundException;
import com.bestrookies.portfolio.repository.PositionRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final PortfolioService portfolioService;

    public PositionService(PositionRepository positionRepository, PortfolioService portfolioService) {
        this.positionRepository = positionRepository;
        this.portfolioService = portfolioService;
    }

    @Transactional
    public PositionResponse createPosition(PositionCreateRequest request) {
        Portfolio portfolio = portfolioService.getPortfolioEntity(request.portfolioId());

        Position position = new Position();
        position.setPortfolio(portfolio);
        position.setAssetType(request.assetType());
        position.setTicker(request.ticker().toUpperCase().trim());
        position.setQuantity(request.quantity());
        position.setAvgCost(request.avgCost());
        position.setCurrency(request.currency().trim().toUpperCase());
        position.setUpdatedAt(Instant.now());

        return toResponse(positionRepository.save(position));
    }

    @Transactional(readOnly = true)
    public Page<PositionResponse> listPositions(Long portfolioId, Pageable pageable) {
        Page<Position> positions = portfolioId == null
            ? positionRepository.findAll(pageable)
            : positionRepository.findByPortfolioId(portfolioId, pageable);

        return positions.map(this::toResponse);
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
}

