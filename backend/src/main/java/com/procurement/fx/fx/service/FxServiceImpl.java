package com.procurement.fx.fx.service;

import com.procurement.fx.fx.client.FrankfurterClient;
import com.procurement.fx.fx.dto.FrankfurterResponse;
import com.procurement.fx.fx.dto.FxConversionResult;
import com.procurement.fx.fx.model.FxRateCache;
import com.procurement.fx.fx.repository.FxRateCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FxServiceImpl implements FxService {

    private static final Logger log = LoggerFactory.getLogger(FxServiceImpl.class);

    public static final int AMOUNT_SCALE = 2;
    public static final int RATE_SCALE = 6;
    public static final String DEFAULT_BASE_CURRENCY = "USD";

    private final FrankfurterClient frankfurterClient;
    private final FxRateCacheRepository fxRateCacheRepository;

    public FxServiceImpl(FrankfurterClient frankfurterClient, FxRateCacheRepository fxRateCacheRepository) {
        this.frankfurterClient = frankfurterClient;
        this.fxRateCacheRepository = fxRateCacheRepository;
    }

    @Override
    @Transactional
    public FxConversionResult convert(BigDecimal amount, String quoteCurrency, String baseCurrency) {
        if (amount == null) {
            return FxConversionResult.failure("Amount cannot be null");
        }

        String from = quoteCurrency != null ? quoteCurrency.trim().toUpperCase() : "";
        String to = (baseCurrency != null && !baseCurrency.isBlank()) ? baseCurrency.trim().toUpperCase() : DEFAULT_BASE_CURRENCY;

        if (from.isEmpty()) {
            return FxConversionResult.failure("Quote currency cannot be empty");
        }

        // Same currency conversion
        if (from.equalsIgnoreCase(to)) {
            BigDecimal converted = amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
            BigDecimal unitRate = BigDecimal.ONE.setScale(RATE_SCALE, RoundingMode.HALF_UP);
            return FxConversionResult.success(converted, unitRate, LocalDate.now(), LocalDateTime.now(), true);
        }

        LocalDate today = LocalDate.now();

        // Check database cache for today's rate
        Optional<FxRateCache> cachedToday = fxRateCacheRepository
                .findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate(from, to, today);

        if (cachedToday.isPresent()) {
            FxRateCache cache = cachedToday.get();
            log.info("FX cache hit for {} -> {} on {}: rate={}", from, to, cache.getRateDate(), cache.getRate());
            BigDecimal converted = computeConvertedAmount(amount, cache.getRate());
            return FxConversionResult.success(converted, cache.getRate(), cache.getRateDate(), cache.getFetchedAt(), true);
        }

        // Cache miss, fetch rate from Frankfurter API
        log.info("FX cache miss for {} -> {} on {}. Calling external Frankfurter API...", from, to, today);
        Optional<FrankfurterResponse> apiResponse = frankfurterClient.fetchLatestRate(from, to);

        if (apiResponse.isPresent()) {
            FrankfurterResponse response = apiResponse.get();
            BigDecimal rate = response.getRates().get(to);
            if (rate != null) {
                LocalDate rateDate = response.getDate() != null ? response.getDate() : today;
                FxRateCache newCache = new FxRateCache(from, to, rate.setScale(RATE_SCALE, RoundingMode.HALF_UP), rateDate);
                newCache = fxRateCacheRepository.save(newCache);
                log.info("Cached new FX rate for {} -> {} ({}) = {}", from, to, rateDate, rate);

                BigDecimal converted = computeConvertedAmount(amount, newCache.getRate());
                return FxConversionResult.success(converted, newCache.getRate(), newCache.getRateDate(), newCache.getFetchedAt(), false);
            }
        }

        // Fallback to latest historical cached rate if external API is unreachable
        log.warn("Frankfurter API failed. Attempting to fall back to latest historical cached rate for {} -> {}", from, to);
        Optional<FxRateCache> latestFallback = fxRateCacheRepository
                .findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseOrderByRateDateDescFetchedAtDesc(from, to);

        if (latestFallback.isPresent()) {
            FxRateCache fallbackCache = latestFallback.get();
            log.warn("Using stale/historical FX rate from {} for {} -> {}: rate={}",
                    fallbackCache.getRateDate(), from, to, fallbackCache.getRate());
            BigDecimal converted = computeConvertedAmount(amount, fallbackCache.getRate());
            return FxConversionResult.success(converted, fallbackCache.getRate(), fallbackCache.getRateDate(),
                    fallbackCache.getFetchedAt(), true);
        }

        // Fallback failed and no cache available
        log.error("Unable to convert {} -> {}. External API failed and no cached rates exist in database.", from, to);
        return FxConversionResult.failure("External FX rate service unavailable and no historical cached rate found.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<FxRateCache> getAllCachedRates() {
        return fxRateCacheRepository.findAllByOrderByFetchedAtDesc();
    }

    private BigDecimal computeConvertedAmount(BigDecimal amount, BigDecimal rate) {
        // Formula per assessment brief: converted_amount = quote_amount * rate(quote_currency -> base_currency)
        return amount.multiply(rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
}
