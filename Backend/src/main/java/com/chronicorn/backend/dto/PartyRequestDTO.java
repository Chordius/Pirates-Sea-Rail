package com.chronicorn.backend.dto;

import java.util.List;
import java.util.UUID;

// Used to request a verification of the active party
public class PartyRequestDTO {
    private UUID userId;
    private List<String> partyCharIds;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public List<String> getPartyCharIds() { return partyCharIds; }
    public void setPartyCharIds(List<String> partyCharIds) { this.partyCharIds = partyCharIds; }
}