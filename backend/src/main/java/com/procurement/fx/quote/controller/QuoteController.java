package com.procurement.fx.quote.controller;

import com.procurement.fx.fx.model.FxRateCache;
import com.procurement.fx.fx.service.FxService;
import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final FxService fxService;

    public QuoteController(QuoteService quoteService, FxService fxService) {
        this.quoteService = quoteService;
        this.fxService = fxService;
    }

    /**
     * List all purchase quotes with optional filters (supplier, currency, budget flag, item code).
     */
    @GetMapping
    public ResponseEntity<List<QuoteResponseDto>> listQuotes(
            @RequestParam(required = false) String supplier,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BudgetFlag budgetFlag,
            @RequestParam(required = false) String itemCode) {
        List<QuoteResponseDto> quotes = quoteService.getAllQuotes(supplier, currency, budgetFlag, itemCode);
        return ResponseEntity.ok(quotes);
    }

    /**
     * Get single purchase quote by ID (404 if not found).
     */
    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> getQuoteById(@PathVariable Long id) {
        QuoteResponseDto quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quote);
    }

    /**
     * Create a new purchase quote with currency conversion and budget check.
     */
    @PostMapping
    public ResponseEntity<QuoteResponseDto> createQuote(@Valid @RequestBody QuoteRequestDto request) {
        QuoteResponseDto created = quoteService.createQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing purchase quote by ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<QuoteResponseDto> updateQuote(
            @PathVariable Long id,
            @Valid @RequestBody QuoteRequestDto request) {
        QuoteResponseDto updated = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a purchase quote by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Recalculate all quotes with current live / cached rates.
     */
    @PostMapping("/recalculate")
    public ResponseEntity<List<QuoteResponseDto>> recalculateAll() {
        List<QuoteResponseDto> updatedList = quoteService.recalculateAllQuotes();
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Auxiliary endpoint to inspect cached FX rates.
     */
    @GetMapping("/rates")
    public ResponseEntity<List<FxRateCache>> getCachedRates() {
        return ResponseEntity.ok(fxService.getAllCachedRates());
    }
}
