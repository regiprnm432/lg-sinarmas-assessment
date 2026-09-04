package com.procurement.fx.fx.repository;

import com.procurement.fx.fx.model.FxRateCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FxRateCacheRepository extends JpaRepository<FxRateCache, Long> {

    Optional<FxRateCache> findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate(
            String baseCurrency, String targetCurrency, LocalDate rateDate);

    Optional<FxRateCache> findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseOrderByRateDateDescFetchedAtDesc(
            String baseCurrency, String targetCurrency);

    List<FxRateCache> findAllByOrderByFetchedAtDesc();
}
