package com.chronicorn.frontend.managers.networkManager;

public class NetworkConfigure {
    public enum Environment {
        OFFLINE,
        LOCAL,
        PROD
    }

    // Configure manually here to switch environments
    public static final Environment ACTIVE_ENV = Environment.LOCAL;

    public static String getBaseUrl() {
        switch (ACTIVE_ENV) {
            case LOCAL:
                return "http://localhost:8080/api";
            case PROD:
                return "https://pirates-sea-rail-production.up.railway.app/api";
            case OFFLINE:
            default:
                return "";
        }
    }

    public static boolean isOffline() {
        return ACTIVE_ENV == Environment.OFFLINE;
    }
}
