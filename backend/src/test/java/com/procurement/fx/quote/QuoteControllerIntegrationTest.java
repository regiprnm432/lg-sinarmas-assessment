package com.procurement.fx.quote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.procurement.fx.quote.dto.QuoteRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/quotes should return list of seeded quotes")
    void testListQuotes() throws Exception {
        mockMvc.perform(get("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].supplierName", notNullValue()))
                .andExpect(jsonPath("$[0].budgetFlag", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/quotes with valid request should return 201 Created and converted values")
    void testCreateQuoteSuccess() throws Exception {
        QuoteRequestDto request = new QuoteRequestDto(
                "LG Innotek Gumi",
                "CAMERA-MOD-48MP",
                new BigDecimal("45.00"),
                "USD",
                "USD",
                new BigDecimal("50.00")
        );

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.supplierName", is("LG Innotek Gumi")))
                .andExpect(jsonPath("$.budgetFlag", is("WITHIN_BUDGET")))
                .andExpect(jsonPath("$.convertedAmount", is(45.00)));
    }

    @Test
    @DisplayName("POST /api/quotes with invalid amount should return 400 Bad Request")
    void testCreateQuoteValidationError() throws Exception {
        QuoteRequestDto invalidRequest = new QuoteRequestDto(
                "", // Blank supplier name
                "ITEM-ERR",
                new BigDecimal("-10.00"), // Negative amount
                "INVALID_CURRENCY", // Invalid currency format
                "USD",
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/api/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.validationErrors", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/quotes/99999 non-existent ID should return 404 Not Found")
    void testGetQuoteNotFound() throws Exception {
        mockMvc.perform(get("/api/quotes/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("not found")));
    }
}
