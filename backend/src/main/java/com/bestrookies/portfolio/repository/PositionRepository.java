package com.bestrookies.portfolio.repository;

import com.bestrookies.portfolio.entity.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByPortfolioIdOrderByIdAsc(Long portfolioId);
}

