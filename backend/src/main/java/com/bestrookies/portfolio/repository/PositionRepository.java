package com.bestrookies.portfolio.repository;

import com.bestrookies.portfolio.entity.AssetType;
import com.bestrookies.portfolio.entity.Position;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PositionRepository extends JpaRepository<Position, Long> {
    Page<Position> findByPortfolioId(Long portfolioId, Pageable pageable);

    @Query("""
        select distinct p.ticker
        from Position p
        where (:assetType is null or p.assetType = :assetType)
        order by p.ticker asc
        """)
    List<String> findDistinctTickersByAssetType(@Param("assetType") AssetType assetType);

    @Transactional
    void deleteByPortfolioId(Long portfolioId);
}
