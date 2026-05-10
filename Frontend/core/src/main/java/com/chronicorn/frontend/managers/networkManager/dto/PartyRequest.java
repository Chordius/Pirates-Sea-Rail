package com.chronicorn.frontend.managers.networkManager.dto;

import java.util.Arrays;
import java.util.List;

public class PartyRequest {
    public String userId;
    public List<String> partyCharIds;

    public PartyRequest() {}

    public PartyRequest(String userId, String[] partyCharIds) {
        this.userId = userId;
        this.partyCharIds = Arrays.asList(partyCharIds);
    }
}
