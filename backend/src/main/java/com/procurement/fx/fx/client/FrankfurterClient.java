package com.procurement.fx.fx.client;

import com.procurement.fx.fx.dto.FrankfurterResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class FrankfurterClient {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FrankfurterClient(RestTemplate restTemplate,
                             @Value("${app.frankfurter.base-url:https://api.frankfurter.dev/v1/latest}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetch latest FX rate from Frankfurter open API.
     * e.g. GET https://api.frankfurter.dev/v1/latest?from=KRW&to=USD
     */
    public Optional<FrankfurterResponse> fetchLatestRate(String fromCurrency, String toCurrency) {
        String url = baseUrl + "?from=" + fromCurrency.toUpperCase() + "&to=" + toCurrency.toUpperCase();
        try {
            log.info("Calling Frankfurter API: {}", url);
            FrankfurterResponse response = restTemplate.getForObject(url, FrankfurterResponse.class);
            if (response != null && response.getRates() != null && response.getRates().containsKey(toCurrency.toUpperCase())) {
                log.info("Frankfurter API returned rate for {} -> {}: {}", fromCurrency, toCurrency, response.getRates().get(toCurrency.toUpperCase()));
                return Optional.of(response);
            }
            log.warn("Frankfurter response did not contain rate for target currency {}", toCurrency);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch exchange rate from Frankfurter API ({}): {}", url, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Unexpected error fetching exchange rate: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
