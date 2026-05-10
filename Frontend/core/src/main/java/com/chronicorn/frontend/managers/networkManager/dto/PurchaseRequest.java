package com.chronicorn.frontend.managers.networkManager.dto;

public class PurchaseRequest {
    public String localUserId;
    public double cost;
    public int currencyAmount;

    public PurchaseRequest() {}

    public PurchaseRequest(String localUserId, double cost, int currencyAmount) {
        this.localUserId = localUserId;
        this.cost = cost;
        this.currencyAmount = currencyAmount;
    }
}
