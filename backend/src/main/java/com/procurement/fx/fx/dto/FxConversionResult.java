package com.procurement.fx.fx.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FxConversionResult {
    private BigDecimal convertedAmount;
    private BigDecimal rate;
    private LocalDate rateDate;
    private LocalDateTime rateFetchedAt;
    private boolean success;
    private boolean cached;
    private String errorMessage;

    public FxConversionResult() {
    }

    public static FxConversionResult success(BigDecimal convertedAmount, BigDecimal rate, LocalDate rateDate, LocalDateTime rateFetchedAt, boolean cached) {
        FxConversionResult result = new FxConversionResult();
        result.convertedAmount = convertedAmount;
        result.rate = rate;
        result.rateDate = rateDate;
        result.rateFetchedAt = rateFetchedAt;
        result.success = true;
        result.cached = cached;
        return result;
    }

    public static FxConversionResult failure(String errorMessage) {
        FxConversionResult result = new FxConversionResult();
        result.success = false;
        result.errorMessage = errorMessage;
        return result;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDate getRateDate() {
        return rateDate;
    }

    public void setRateDate(LocalDate rateDate) {
        this.rateDate = rateDate;
    }

    public LocalDateTime getRateFetchedAt() {
        return rateFetchedAt;
    }

    public void setRateFetchedAt(LocalDateTime rateFetchedAt) {
        this.rateFetchedAt = rateFetchedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
