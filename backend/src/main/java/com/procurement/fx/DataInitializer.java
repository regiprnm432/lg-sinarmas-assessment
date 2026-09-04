package com.procurement.fx;

import com.procurement.fx.quote.dto.QuoteRequestDto;
import com.procurement.fx.quote.repository.PurchaseQuoteRepository;
import com.procurement.fx.quote.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PurchaseQuoteRepository quoteRepository;
    private final QuoteService quoteService;

    public DataInitializer(PurchaseQuoteRepository quoteRepository, QuoteService quoteService) {
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
    }

    @Override
    public void run(String... args) {
        if (quoteRepository.count() == 0) {
            log.info("Initializing sample procurement quotes...");

            // Item 1: OLED-PANEL-55 (Compare 3 suppliers with KRW, EUR, USD)
            quoteService.createQuote(new QuoteRequestDto(
                    "LG Display Paju",
                    "OLED-PANEL-55",
                    new BigDecimal("320000.00"), // KRW (~$235 USD)
                    "KRW",
                    "USD",
                    new BigDecimal("280.00")    // Budget $280 -> WITHIN_BUDGET
            ));

            quoteService.createQuote(new QuoteRequestDto(
                    "Samsung SDI Cheonan",
                    "OLED-PANEL-55",
                    new BigDecimal("420000.00"), // KRW (~$309 USD)
                    "KRW",
                    "USD",
                    new BigDecimal("280.00")    // Budget $280 -> OVER_BUDGET
            ));

            quoteService.createQuote(new QuoteRequestDto(
                    "Sharp Sakai Plant",
                    "OLED-PANEL-55",
                    new BigDecimal("260.00"),    // USD
                    "USD",
                    "USD",
                    new BigDecimal("280.00")    // Budget $280 -> WITHIN_BUDGET
            ));

            // Item 2: SMT-CAPACITOR-100UF (Compare 2 suppliers with EUR and IDR)
            quoteService.createQuote(new QuoteRequestDto(
                    "Murata Manufacturing Europe",
                    "SMT-CAP-100UF",
                    new BigDecimal("85.00"),    // EUR (~$98 USD)
                    "EUR",
                    "USD",
                    new BigDecimal("90.00")     // Budget $90 -> OVER_BUDGET
            ));

            quoteService.createQuote(new QuoteRequestDto(
                    "Astra Otoparts Bekasi",
                    "SMT-CAP-100UF",
                    new BigDecimal("1200000.00"), // IDR (~$68 USD)
                    "IDR",
                    "USD",
                    new BigDecimal("90.00")      // Budget $90 -> WITHIN_BUDGET (Cheapest!)
            ));

            log.info("Sample procurement quotes seeded successfully.");
        }
    }
}
