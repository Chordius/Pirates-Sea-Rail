package com.chronicorn.backend.dto;

public class GachaResultDTO {
    private String pulledCharId;
    private boolean isNew;

    public GachaResultDTO(String pulledCharId, boolean isNew) {
        this.pulledCharId = pulledCharId;
        this.isNew = isNew;
    }
    public String getPulledCharId() { return pulledCharId; }
    public boolean isNew() { return isNew; }
}
