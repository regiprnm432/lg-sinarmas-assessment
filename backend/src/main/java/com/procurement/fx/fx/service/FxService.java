package com.procurement.fx.fx.service;

import com.procurement.fx.fx.dto.FxConversionResult;
import com.procurement.fx.fx.model.FxRateCache;

import java.math.BigDecimal;
import java.util.List;

public interface FxService {

    /**
     * Convert an amount from quoteCurrency to baseCurrency.
     * Uses DB cache if available for current day.
     * If cache misses, calls Frankfurter API and caches the result.
     * If API fails, falls back to latest cached rate, or returns failure if no cache exists.
     */
    FxConversionResult convert(BigDecimal amount, String quoteCurrency, String baseCurrency);

    /**
     * Get all cached FX rates for administrative/monitoring purposes.
     */
    List<FxRateCache> getAllCachedRates();
}
