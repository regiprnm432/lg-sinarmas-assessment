package com.procurement.fx.quote.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class QuoteRequestDto {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 100, message = "Supplier name cannot exceed 100 characters")
    private String supplierName;

    @NotBlank(message = "Item code is required")
    @Size(max = 50, message = "Item code cannot exceed 50 characters")
    private String itemCode;

    @NotNull(message = "Quote amount is required")
    @Positive(message = "Quote amount must be greater than zero")
    private BigDecimal quoteAmount;

    @NotBlank(message = "Quote currency is required")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Quote currency must be a 3-letter ISO currency code (e.g. USD, EUR, KRW)")
    private String quoteCurrency;

    @Pattern(regexp = "^[a-zA-Z]{3}$|^$", message = "Base currency must be a 3-letter ISO currency code or empty")
    private String baseCurrency = "USD";

    @NotNull(message = "Budget amount is required")
    @Positive(message = "Budget amount must be greater than zero")
    private BigDecimal budgetAmount;

    public QuoteRequestDto() {
    }

    public QuoteRequestDto(String supplierName, String itemCode, BigDecimal quoteAmount, String quoteCurrency, String baseCurrency, BigDecimal budgetAmount) {
        this.supplierName = supplierName;
        this.itemCode = itemCode;
        this.quoteAmount = quoteAmount;
        this.quoteCurrency = quoteCurrency;
        this.baseCurrency = baseCurrency != null && !baseCurrency.isBlank() ? baseCurrency : "USD";
        this.budgetAmount = budgetAmount;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }

    public void setQuoteAmount(BigDecimal quoteAmount) {
        this.quoteAmount = quoteAmount;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public void setQuoteCurrency(String quoteCurrency) {
        this.quoteCurrency = quoteCurrency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
}
