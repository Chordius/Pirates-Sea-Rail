package com.chronicorn.backend.dto;

import java.util.UUID;

public class PurchaseRequestDTO {
    private UUID localUserId;
    private java.math.BigDecimal cost;
    private int currencyAmount;

    public UUID getLocalUserId() { return localUserId; }
    public void setLocalUserId(UUID localUserId) { this.localUserId = localUserId; }
    public java.math.BigDecimal getCost() { return cost; }
    public void setCost(java.math.BigDecimal cost) { this.cost = cost; }
    public int getCurrencyAmount() { return currencyAmount; }
    public void setCurrencyAmount(int currencyAmount) { this.currencyAmount = currencyAmount; }
}
