package com.procurement.fx.quote;

import com.procurement.fx.common.exception.ResourceNotFoundException;
import com.procurement.fx.fx.dto.FxConversionResult;
import com.procurement.fx.fx.service.FxService;
import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.model.PurchaseQuote;
import com.procurement.fx.quote.repository.PurchaseQuoteRepository;
import com.procurement.fx.quote.service.QuoteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private PurchaseQuoteRepository quoteRepository;

    @Mock
    private FxService fxService;

    private QuoteServiceImpl quoteService;

    @BeforeEach
    void setUp() {
        quoteService = new QuoteServiceImpl(quoteRepository, fxService, "USD");
    }

    @Test
    @DisplayName("Create quote within budget should set flag to WITHIN_BUDGET")
    void testCreateQuoteWithinBudget() {
        QuoteRequestDto request = new QuoteRequestDto(
                "Supplier A", "ITEM-01",
                new BigDecimal("100.00"), "EUR", "USD",
                new BigDecimal("120.00") // Budget is 120 USD
        );

        // Mock conversion: 100 EUR = 108.50 USD
        FxConversionResult fxResult = FxConversionResult.success(
                new BigDecimal("108.50"),
                new BigDecimal("1.085000"),
                LocalDate.now(),
                LocalDateTime.now(),
                false
        );
        when(fxService.convert(any(), any(), any())).thenReturn(fxResult);

        when(quoteRepository.save(any(PurchaseQuote.class))).thenAnswer(invocation -> {
            PurchaseQuote q = invocation.getArgument(0);
            q.setId(1L);
            return q;
        });

        QuoteResponseDto response = quoteService.createQuote(request);

        assertNotNull(response);
        assertEquals(BudgetFlag.WITHIN_BUDGET, response.getBudgetFlag());
        assertEquals(new BigDecimal("108.50"), response.getConvertedAmount());
        assertEquals(new BigDecimal("1.085000"), response.getRateUsed());
    }

    @Test
    @DisplayName("Create quote exceeding budget should set flag to OVER_BUDGET")
    void testCreateQuoteOverBudget() {
        QuoteRequestDto request = new QuoteRequestDto(
                "Supplier B", "ITEM-01",
                new BigDecimal("150.00"), "EUR", "USD",
                new BigDecimal("120.00") // Budget is 120 USD
        );

        // Mock conversion: 150 EUR = 162.75 USD > 120 USD
        FxConversionResult fxResult = FxConversionResult.success(
                new BigDecimal("162.75"),
                new BigDecimal("1.085000"),
                LocalDate.now(),
                LocalDateTime.now(),
                false
        );
        when(fxService.convert(any(), any(), any())).thenReturn(fxResult);

        when(quoteRepository.save(any(PurchaseQuote.class))).thenAnswer(invocation -> {
            PurchaseQuote q = invocation.getArgument(0);
            q.setId(2L);
            return q;
        });

        QuoteResponseDto response = quoteService.createQuote(request);

        assertNotNull(response);
        assertEquals(BudgetFlag.OVER_BUDGET, response.getBudgetFlag());
        assertEquals(new BigDecimal("162.75"), response.getConvertedAmount());
    }

    @Test
    @DisplayName("When FX service fails, quote should be flagged as UNKNOWN without crashing")
    void testCreateQuoteWithFxFailureSetsUnknown() {
        QuoteRequestDto request = new QuoteRequestDto(
                "Supplier C", "ITEM-02",
                new BigDecimal("5000.00"), "KRW", "USD",
                new BigDecimal("10.00")
        );

        // Mock FX failure
        FxConversionResult fxResult = FxConversionResult.failure("External API unreachable");
        when(fxService.convert(any(), any(), any())).thenReturn(fxResult);

        when(quoteRepository.save(any(PurchaseQuote.class))).thenAnswer(invocation -> {
            PurchaseQuote q = invocation.getArgument(0);
            q.setId(3L);
            return q;
        });

        QuoteResponseDto response = quoteService.createQuote(request);

        assertNotNull(response);
        assertEquals(BudgetFlag.UNKNOWN, response.getBudgetFlag());
        assertNull(response.getConvertedAmount());
        assertNull(response.getRateUsed());
    }

    @Test
    @DisplayName("Get quote by non-existent ID should throw ResourceNotFoundException (404)")
    void testGetQuoteNotFoundThrowsException() {
        when(quoteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> quoteService.getQuoteById(999L));
    }

    @Test
    @DisplayName("Quotes for the same item should mark the lowest converted amount as cheapest")
    void testCheapestQuoteComparison() {
        PurchaseQuote q1 = new PurchaseQuote();
        q1.setId(1L);
        q1.setItemCode("CHIP-500");
        q1.setSupplierName("Supplier X");
        q1.setQuoteAmount(new BigDecimal("100"));
        q1.setConvertedAmount(new BigDecimal("100.00")); // Higher
        q1.setBudgetAmount(new BigDecimal("120.00"));
        q1.setBudgetFlag(BudgetFlag.WITHIN_BUDGET);

        PurchaseQuote q2 = new PurchaseQuote();
        q2.setId(2L);
        q2.setItemCode("CHIP-500");
        q2.setSupplierName("Supplier Y");
        q2.setQuoteAmount(new BigDecimal("80"));
        q2.setConvertedAmount(new BigDecimal("80.00")); // Lowest / Cheapest!
        q2.setBudgetAmount(new BigDecimal("120.00"));
        q2.setBudgetFlag(BudgetFlag.WITHIN_BUDGET);

        when(quoteRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(q1, q2));

        List<QuoteResponseDto> result = quoteService.getAllQuotes(null, null, null, null);

        assertEquals(2, result.size());
        assertFalse(result.get(0).isCheapest());
        assertTrue(result.get(1).isCheapest());
    }
}
