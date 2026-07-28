package com.bestrookies.portfolio.repository;

import com.bestrookies.portfolio.entity.PriceSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    Optional<PriceSnapshot> findTopByTickerOrderByFetchedAtDesc(String ticker);
}
