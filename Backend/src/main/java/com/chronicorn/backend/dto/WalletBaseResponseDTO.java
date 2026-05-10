package com.chronicorn.backend.dto;

import java.util.Map;

// 3. Used to parse the exact Node.js "baseResponse" JSON structure
public class WalletBaseResponseDTO {
    private boolean success;
    private String message;
    private Map<String, String> payload;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Map<String, String> getPayload() { return payload; }
    public void setPayload(Map<String, String> payload) { this.payload = payload; }
}