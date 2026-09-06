package com.procurement.fx.quote.controller;

import com.procurement.fx.fx.model.FxRateCache;
import com.procurement.fx.fx.service.FxService;
import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@Tag(name = "Purchase Quotes", description = "Endpoints for managing supplier quotes, currency conversions, and FX cache")
public class QuoteController {

    private final QuoteService quoteService;
    private final FxService fxService;

    public QuoteController(QuoteService quoteService, FxService fxService) {
        this.quoteService = quoteService;
        this.fxService = fxService;
    }

    @GetMapping
    @Operation(summary = "List quotes with optional filters", description = "Retrieve all purchase quotes, optionally filtered by supplier name, quote currency, budget status, or item code. Automatically calculates the cheapest supplier quote per item code.")
    public ResponseEntity<List<QuoteResponseDto>> listQuotes(
            @Parameter(description = "Filter by supplier name (partial match)") @RequestParam(required = false) String supplier,
            @Parameter(description = "Filter by quote currency code (e.g. KRW, EUR, IDR)") @RequestParam(required = false) String currency,
            @Parameter(description = "Filter by budget status: WITHIN_BUDGET, OVER_BUDGET, or UNKNOWN") @RequestParam(required = false) BudgetFlag budgetFlag,
            @Parameter(description = "Filter by item code (partial match)") @RequestParam(required = false) String itemCode) {
        List<QuoteResponseDto> quotes = quoteService.getAllQuotes(supplier, currency, budgetFlag, itemCode);
        return ResponseEntity.ok(quotes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quote by ID", description = "Retrieve a single purchase quote by its database identifier. Returns 404 if not found.")
    public ResponseEntity<QuoteResponseDto> getQuoteById(
            @Parameter(description = "Quote ID") @PathVariable Long id) {
        QuoteResponseDto quote = quoteService.getQuoteById(id);
        return ResponseEntity.ok(quote);
    }

    @PostMapping
    @Operation(summary = "Create purchase quote", description = "Create a new supplier quote. Automatically fetches live/cached exchange rates, converts amount to base currency (USD), and flags status as WITHIN_BUDGET, OVER_BUDGET, or UNKNOWN.")
    public ResponseEntity<QuoteResponseDto> createQuote(@Valid @RequestBody QuoteRequestDto request) {
        QuoteResponseDto created = quoteService.createQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update purchase quote", description = "Update an existing quote and recalculate its currency conversion and budget flag.")
    public ResponseEntity<QuoteResponseDto> updateQuote(
            @Parameter(description = "Quote ID") @PathVariable Long id,
            @Valid @RequestBody QuoteRequestDto request) {
        QuoteResponseDto updated = quoteService.updateQuote(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete purchase quote", description = "Delete a purchase quote by its identifier. Returns 204 No Content on success.")
    public ResponseEntity<Void> deleteQuote(
            @Parameter(description = "Quote ID") @PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalculate all quotes", description = "Re-fetch latest exchange rates and re-convert all existing quotes in bulk.")
    public ResponseEntity<List<QuoteResponseDto>> recalculateAll() {
        List<QuoteResponseDto> updatedList = quoteService.recalculateAllQuotes();
        return ResponseEntity.ok(updatedList);
    }

    @GetMapping("/rates")
    @Operation(summary = "View cached FX rates", description = "Inspect the current contents of the fx_rate_cache table.")
    public ResponseEntity<List<FxRateCache>> getCachedRates() {
        return ResponseEntity.ok(fxService.getAllCachedRates());
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Export quotes comparison to Excel (.xlsx)", description = "Export supplier quotes comparison table to Microsoft Excel format (.xlsx) with formatting and styling, optionally filtered by supplier, currency, status, or item.")
    public ResponseEntity<byte[]> exportToExcel(
            @Parameter(description = "Filter by supplier name (partial match)") @RequestParam(required = false) String supplier,
            @Parameter(description = "Filter by quote currency code (e.g. KRW, EUR, IDR)") @RequestParam(required = false) String currency,
            @Parameter(description = "Filter by budget status: WITHIN_BUDGET, OVER_BUDGET, or UNKNOWN") @RequestParam(required = false) BudgetFlag budgetFlag,
            @Parameter(description = "Filter by item code (partial match)") @RequestParam(required = false) String itemCode) {
        byte[] excelBytes = quoteService.exportQuotesToExcel(supplier, currency, budgetFlag, itemCode);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"supplier_quotes_comparison.xlsx\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }
}

