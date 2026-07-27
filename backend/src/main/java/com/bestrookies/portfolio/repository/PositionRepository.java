package com.bestrookies.portfolio.repository;

import com.bestrookies.portfolio.entity.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Page<Position> findByPortfolioId(Long portfolioId, Pageable pageable);
}

