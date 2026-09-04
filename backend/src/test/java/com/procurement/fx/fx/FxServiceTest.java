package com.procurement.fx.fx;

import com.procurement.fx.fx.client.FrankfurterClient;
import com.procurement.fx.fx.dto.FrankfurterResponse;
import com.procurement.fx.fx.dto.FxConversionResult;
import com.procurement.fx.fx.model.FxRateCache;
import com.procurement.fx.fx.repository.FxRateCacheRepository;
import com.procurement.fx.fx.service.FxServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FxServiceTest {

    @Mock
    private FrankfurterClient frankfurterClient;

    @Mock
    private FxRateCacheRepository fxRateCacheRepository;

    private FxServiceImpl fxService;

    @BeforeEach
    void setUp() {
        fxService = new FxServiceImpl(frankfurterClient, fxRateCacheRepository);
    }

    @Test
    @DisplayName("Same currency conversion should return 1.0 rate without calling API or DB")
    void testSameCurrencyConversion() {
        FxConversionResult result = fxService.convert(new BigDecimal("100.00"), "USD", "USD");

        assertTrue(result.isSuccess());
        assertEquals(new BigDecimal("100.00"), result.getConvertedAmount());
        assertEquals(new BigDecimal("1.000000"), result.getRate());
        verifyNoInteractions(frankfurterClient);
        verifyNoInteractions(fxRateCacheRepository);
    }

    @Test
    @DisplayName("Cache hit: should use rate from DB without calling Frankfurter API")
    void testCacheHit() {
        LocalDate today = LocalDate.now();
        FxRateCache cachedRate = new FxRateCache("KRW", "USD", new BigDecimal("0.000750"), today);
        cachedRate.setFetchedAt(LocalDateTime.now().minusHours(2));

        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate("KRW", "USD", today))
                .thenReturn(Optional.of(cachedRate));

        // 100,000 KRW * 0.000750 = 75.00 USD
        FxConversionResult result = fxService.convert(new BigDecimal("100000.00"), "KRW", "USD");

        assertTrue(result.isSuccess());
        assertTrue(result.isCached());
        assertEquals(new BigDecimal("75.00"), result.getConvertedAmount());
        assertEquals(new BigDecimal("0.000750"), result.getRate());
        verify(frankfurterClient, never()).fetchLatestRate(anyString(), anyString());
    }

    @Test
    @DisplayName("Cache miss: should call Frankfurter API and persist new rate in DB")
    void testCacheMissCallsApiAndPersists() {
        LocalDate today = LocalDate.now();
        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate("EUR", "USD", today))
                .thenReturn(Optional.empty());

        FrankfurterResponse apiResponse = new FrankfurterResponse();
        apiResponse.setBase("EUR");
        apiResponse.setDate(today);
        apiResponse.setRates(Map.of("USD", new BigDecimal("1.085000")));

        when(frankfurterClient.fetchLatestRate("EUR", "USD")).thenReturn(Optional.of(apiResponse));

        FxRateCache savedCache = new FxRateCache("EUR", "USD", new BigDecimal("1.085000"), today);
        when(fxRateCacheRepository.save(any(FxRateCache.class))).thenReturn(savedCache);

        // 100 EUR * 1.085000 = 108.50 USD
        FxConversionResult result = fxService.convert(new BigDecimal("100.00"), "EUR", "USD");

        assertTrue(result.isSuccess());
        assertFalse(result.isCached());
        assertEquals(new BigDecimal("108.50"), result.getConvertedAmount());
        assertEquals(new BigDecimal("1.085000"), result.getRate());
        verify(frankfurterClient, times(1)).fetchLatestRate("EUR", "USD");
        verify(fxRateCacheRepository, times(1)).save(any(FxRateCache.class));
    }

    @Test
    @DisplayName("Resilience: When API fails, should fall back to historical cached rate")
    void testApiFailureFallsBackToHistoricalCache() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate("IDR", "USD", today))
                .thenReturn(Optional.empty());

        // API fails
        when(frankfurterClient.fetchLatestRate("IDR", "USD")).thenReturn(Optional.empty());

        // Historical cache exists
        FxRateCache historicalCache = new FxRateCache("IDR", "USD", new BigDecimal("0.000062"), yesterday);
        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseOrderByRateDateDescFetchedAtDesc("IDR", "USD"))
                .thenReturn(Optional.of(historicalCache));

        // 1,000,000 IDR * 0.000062 = 62.00 USD
        FxConversionResult result = fxService.convert(new BigDecimal("1000000.00"), "IDR", "USD");

        assertTrue(result.isSuccess());
        assertTrue(result.isCached());
        assertEquals(new BigDecimal("62.00"), result.getConvertedAmount());
        assertEquals(new BigDecimal("0.000062"), result.getRate());
    }

    @Test
    @DisplayName("Resilience: When API fails and no cache exists, should return failure gracefully without exception")
    void testApiFailureWithNoCacheReturnsFailureGracefully() {
        LocalDate today = LocalDate.now();

        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseAndRateDate("XYZ", "USD", today))
                .thenReturn(Optional.empty());
        when(frankfurterClient.fetchLatestRate("XYZ", "USD")).thenReturn(Optional.empty());
        when(fxRateCacheRepository.findFirstByBaseCurrencyIgnoreCaseAndTargetCurrencyIgnoreCaseOrderByRateDateDescFetchedAtDesc("XYZ", "USD"))
                .thenReturn(Optional.empty());

        FxConversionResult result = fxService.convert(new BigDecimal("500.00"), "XYZ", "USD");

        assertFalse(result.isSuccess());
        assertNull(result.getConvertedAmount());
        assertNotNull(result.getErrorMessage());
    }
}
