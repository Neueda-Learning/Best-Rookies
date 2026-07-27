package com.bestrookies.portfolio.repository;

import com.bestrookies.portfolio.entity.PriceSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    /** Returns the most recent price snapshot for a given ticker. */
    @Query("SELECT p FROM PriceSnapshot p WHERE p.ticker = :ticker ORDER BY p.fetchedAt DESC LIMIT 1")
    Optional<PriceSnapshot> findLatestByTicker(@Param("ticker") String ticker);
}

