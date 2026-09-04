package com.procurement.fx.quote.service;

import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;

import java.util.List;

public interface QuoteService {

    QuoteResponseDto createQuote(QuoteRequestDto request);

    List<QuoteResponseDto> getAllQuotes(String supplier, String currency, BudgetFlag budgetFlag, String itemCode);

    QuoteResponseDto getQuoteById(Long id);

    QuoteResponseDto updateQuote(Long id, QuoteRequestDto request);

    void deleteQuote(Long id);

    List<QuoteResponseDto> recalculateAllQuotes();
}
