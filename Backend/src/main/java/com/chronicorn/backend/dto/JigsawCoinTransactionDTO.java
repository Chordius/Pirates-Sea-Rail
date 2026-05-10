package com.chronicorn.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;

public class JigsawCoinTransactionDTO {

    @JsonProperty("global_user_id")
    private UUID globalUserId;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("reference_id")
    private String referenceId;

    public JigsawCoinTransactionDTO(UUID globalUserId, BigDecimal amount, String referenceId) {
        this.globalUserId = globalUserId;
        this.amount = amount;
        this.referenceId = referenceId;
    }

    // Getters and Setters
    public UUID getGlobalUserId() { return globalUserId; }
    public void setGlobalUserId(UUID globalUserId) { this.globalUserId = globalUserId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
}