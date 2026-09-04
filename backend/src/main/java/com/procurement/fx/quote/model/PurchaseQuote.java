package com.procurement.fx.quote.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_quote")
public class PurchaseQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_name", length = 100, nullable = false)
    private String supplierName;

    @Column(name = "item_code", length = 50, nullable = false)
    private String itemCode;

    @Column(name = "quote_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal quoteAmount;

    @Column(name = "quote_currency", length = 3, nullable = false)
    private String quoteCurrency;

    @Column(name = "base_currency", length = 3, nullable = false)
    private String baseCurrency = "USD";

    @Column(name = "budget_amount", precision = 14, scale = 2, nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "converted_amount", precision = 14, scale = 2)
    private BigDecimal convertedAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "budget_flag", length = 15)
    private BudgetFlag budgetFlag;

    @Column(name = "rate_used", precision = 18, scale = 6)
    private BigDecimal rateUsed;

    @Column(name = "rate_fetched_at")
    private LocalDateTime rateFetchedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PurchaseQuote() {
        this.createdAt = LocalDateTime.now();
        this.baseCurrency = "USD";
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
        this.quoteCurrency = quoteCurrency != null ? quoteCurrency.toUpperCase() : null;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency != null ? baseCurrency.toUpperCase() : "USD";
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
}
