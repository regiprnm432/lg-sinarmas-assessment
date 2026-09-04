package com.procurement.fx.quote.dto;

import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.model.PurchaseQuote;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuoteResponseDto {

    private Long id;
    private String supplierName;
    private String itemCode;
    private BigDecimal quoteAmount;
    private String quoteCurrency;
    private String baseCurrency;
    private BigDecimal budgetAmount;
    private BigDecimal convertedAmount;
    private BudgetFlag budgetFlag;
    private BigDecimal rateUsed;
    private LocalDateTime rateFetchedAt;
    private LocalDateTime createdAt;
    private boolean cheapest;

    public QuoteResponseDto() {
    }

    public static QuoteResponseDto fromEntity(PurchaseQuote entity) {
        QuoteResponseDto dto = new QuoteResponseDto();
        dto.setId(entity.getId());
        dto.setSupplierName(entity.getSupplierName());
        dto.setItemCode(entity.getItemCode());
        dto.setQuoteAmount(entity.getQuoteAmount());
        dto.setQuoteCurrency(entity.getQuoteCurrency());
        dto.setBaseCurrency(entity.getBaseCurrency());
        dto.setBudgetAmount(entity.getBudgetAmount());
        dto.setConvertedAmount(entity.getConvertedAmount());
        dto.setBudgetFlag(entity.getBudgetFlag());
        dto.setRateUsed(entity.getRateUsed());
        dto.setRateFetchedAt(entity.getRateFetchedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCheapest(false);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public BudgetFlag getBudgetFlag() {
        return budgetFlag;
    }

    public void setBudgetFlag(BudgetFlag budgetFlag) {
        this.budgetFlag = budgetFlag;
    }

    public BigDecimal getRateUsed() {
        return rateUsed;
    }

    public void setRateUsed(BigDecimal rateUsed) {
        this.rateUsed = rateUsed;
    }

    public LocalDateTime getRateFetchedAt() {
        return rateFetchedAt;
    }

    public void setRateFetchedAt(LocalDateTime rateFetchedAt) {
        this.rateFetchedAt = rateFetchedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCheapest() {
        return cheapest;
    }

    public void setCheapest(boolean cheapest) {
        this.cheapest = cheapest;
    }
}
