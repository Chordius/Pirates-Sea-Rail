package com.chronicorn.frontend.battlers;

import com.chronicorn.frontend.battlers.parties.Deal;
import com.chronicorn.frontend.battlers.parties.Porter;
import com.chronicorn.frontend.battlers.parties.Reyna;
import com.chronicorn.frontend.battlers.parties.Sailor;

public class ActorFactory {
    
    public static Actor createActor(String charId) {
        if (charId == null) {
            return new Sailor(); // Default character if charId is null
        }
        
        switch (charId.toUpperCase()) {
            case "C001":
                return new Sailor();
            case "C002":
                return new Porter();
            case "C003":
                return new Reyna();
            case "C004":
                return new Deal();
            default:
                // Provide a default fallback character
                return new Sailor();
        }
    }
}