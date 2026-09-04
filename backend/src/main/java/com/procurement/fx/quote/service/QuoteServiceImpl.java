package com.procurement.fx.quote.service;

import com.procurement.fx.common.exception.ResourceNotFoundException;
import com.procurement.fx.fx.dto.FxConversionResult;
import com.procurement.fx.fx.service.FxService;
import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.model.PurchaseQuote;
import com.procurement.fx.quote.repository.PurchaseQuoteRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl implements QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteServiceImpl.class);

    private final PurchaseQuoteRepository quoteRepository;
    private final FxService fxService;
    private final String defaultBaseCurrency;

    public QuoteServiceImpl(PurchaseQuoteRepository quoteRepository,
                            FxService fxService,
                            @Value("${app.base-currency:USD}") String defaultBaseCurrency) {
        this.quoteRepository = quoteRepository;
        this.fxService = fxService;
        this.defaultBaseCurrency = defaultBaseCurrency;
    }

    @Override
    @Transactional
    public QuoteResponseDto createQuote(QuoteRequestDto request) {
        PurchaseQuote quote = new PurchaseQuote();
        populateAndCalculateQuote(quote, request);
        PurchaseQuote saved = quoteRepository.save(quote);
        log.info("Created quote id={} supplier='{}' item='{}' flag={}",
                saved.getId(), saved.getSupplierName(), saved.getItemCode(), saved.getBudgetFlag());
        return QuoteResponseDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponseDto> getAllQuotes(String supplier, String currency, BudgetFlag budgetFlag, String itemCode) {
        String cleanSupplier = (supplier != null && !supplier.isBlank()) ? supplier.trim() : null;
        String cleanCurrency = (currency != null && !currency.isBlank()) ? currency.trim().toUpperCase() : null;
        String cleanItemCode = (itemCode != null && !itemCode.isBlank()) ? itemCode.trim() : null;

        List<PurchaseQuote> quotes;
        if (cleanSupplier == null && cleanCurrency == null && budgetFlag == null && cleanItemCode == null) {
            quotes = quoteRepository.findAllByOrderByCreatedAtDesc();
        } else {
            Specification<PurchaseQuote> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (cleanSupplier != null) {
                    predicates.add(cb.like(cb.lower(root.get("supplierName")), "%" + cleanSupplier.toLowerCase() + "%"));
                }
                if (cleanCurrency != null) {
                    predicates.add(cb.equal(cb.upper(root.get("quoteCurrency")), cleanCurrency));
                }
                if (budgetFlag != null) {
                    predicates.add(cb.equal(root.get("budgetFlag"), budgetFlag));
                }
                if (cleanItemCode != null) {
                    predicates.add(cb.like(cb.lower(root.get("itemCode")), "%" + cleanItemCode.toLowerCase() + "%"));
                }
                return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
            };
            quotes = quoteRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        List<QuoteResponseDto> dtos = quotes.stream()
                .map(QuoteResponseDto::fromEntity)
                .collect(Collectors.toList());

        // Mark the cheapest quote(s) per item_code in base currency (P2 Requirement)
        markCheapestQuotes(dtos);

        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponseDto getQuoteById(Long id) {
        PurchaseQuote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase quote not found with ID: " + id));
        QuoteResponseDto dto = QuoteResponseDto.fromEntity(quote);

        // Check cheapest for this single item among all existing quotes
        List<PurchaseQuote> sameItemQuotes = quoteRepository.findByItemCode(quote.getItemCode());
        BigDecimal minAmount = sameItemQuotes.stream()
                .map(PurchaseQuote::getConvertedAmount)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);

        if (minAmount != null && quote.getConvertedAmount() != null &&
                quote.getConvertedAmount().compareTo(minAmount) == 0) {
            dto.setCheapest(true);
        }

        return dto;
    }

    @Override
    @Transactional
    public QuoteResponseDto updateQuote(Long id, QuoteRequestDto request) {
        PurchaseQuote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase quote not found with ID: " + id));

        populateAndCalculateQuote(quote, request);
        PurchaseQuote updated = quoteRepository.save(quote);
        log.info("Updated quote id={} flag={}", updated.getId(), updated.getBudgetFlag());

        return QuoteResponseDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteQuote(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Purchase quote not found with ID: " + id);
        }
        quoteRepository.deleteById(id);
        log.info("Deleted quote id={}", id);
    }

    @Override
    @Transactional
    public List<QuoteResponseDto> recalculateAllQuotes() {
        List<PurchaseQuote> allQuotes = quoteRepository.findAll();
        for (PurchaseQuote quote : allQuotes) {
            recalculateQuote(quote);
        }
        quoteRepository.saveAll(allQuotes);
        return getAllQuotes(null, null, null, null);
    }

    private void populateAndCalculateQuote(PurchaseQuote quote, QuoteRequestDto request) {
        quote.setSupplierName(request.getSupplierName().trim());
        quote.setItemCode(request.getItemCode().trim().toUpperCase());
        quote.setQuoteAmount(request.getQuoteAmount());
        quote.setQuoteCurrency(request.getQuoteCurrency().trim().toUpperCase());

        String base = (request.getBaseCurrency() != null && !request.getBaseCurrency().isBlank())
                ? request.getBaseCurrency().trim().toUpperCase()
                : defaultBaseCurrency;
        quote.setBaseCurrency(base);
        quote.setBudgetAmount(request.getBudgetAmount());

        recalculateQuote(quote);
    }

    private void recalculateQuote(PurchaseQuote quote) {
        FxConversionResult fxResult = fxService.convert(
                quote.getQuoteAmount(),
                quote.getQuoteCurrency(),
                quote.getBaseCurrency()
        );

        if (fxResult.isSuccess()) {
            quote.setConvertedAmount(fxResult.getConvertedAmount());
            quote.setRateUsed(fxResult.getRate());
            quote.setRateFetchedAt(fxResult.getRateFetchedAt());

            // Compute rule per assessment brief:
            // if converted_amount > budget_amount then OVER_BUDGET, else WITHIN_BUDGET
            if (quote.getConvertedAmount().compareTo(quote.getBudgetAmount()) > 0) {
                quote.setBudgetFlag(BudgetFlag.OVER_BUDGET);
            } else {
                quote.setBudgetFlag(BudgetFlag.WITHIN_BUDGET);
            }
        } else {
            // If rate API is down and no cache exists: flag as UNKNOWN (do not crash)
            quote.setConvertedAmount(null);
            quote.setRateUsed(null);
            quote.setRateFetchedAt(null);
            quote.setBudgetFlag(BudgetFlag.UNKNOWN);
            log.warn("Setting budget flag to UNKNOWN for quote id={} due to FX conversion failure: {}",
                    quote.getId(), fxResult.getErrorMessage());
        }
    }

    private void markCheapestQuotes(List<QuoteResponseDto> dtos) {
        // Group by itemCode
        Map<String, List<QuoteResponseDto>> itemGroups = dtos.stream()
                .filter(q -> q.getItemCode() != null)
                .collect(Collectors.groupingBy(q -> q.getItemCode().toUpperCase()));

        for (List<QuoteResponseDto> group : itemGroups.values()) {
            // Find minimum converted_amount in this group (ignoring null converted amounts)
            Optional<BigDecimal> minConverted = group.stream()
                    .map(QuoteResponseDto::getConvertedAmount)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo);

            if (minConverted.isPresent()) {
                BigDecimal min = minConverted.get();
                for (QuoteResponseDto q : group) {
                    if (q.getConvertedAmount() != null && q.getConvertedAmount().compareTo(min) == 0) {
                        q.setCheapest(true);
                    }
                }
            }
        }
    }
}
