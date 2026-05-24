package com.chronicorn.frontend.managers.networkManager.dto;

public class PartyRequest {
    public String userId;
    public String[] partyCharIds;

    public PartyRequest() {}

    public PartyRequest(String userId, String[] partyCharIds) {
        this.userId = userId;
        this.partyCharIds = partyCharIds;
    }
}
