package com.chronicorn.backend.services;

import com.chronicorn.backend.dto.UserAuthRequestDTO;
import com.chronicorn.backend.dto.UserAuthResponseDTO;
import com.chronicorn.backend.dto.WalletBaseResponseDTO;
import com.chronicorn.backend.models.User;
import com.chronicorn.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Value("${wallet.api.url}")
    private String walletApiUrl;

    @Value("${wallet.api.key}")
    private String walletApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public UserAuthResponseDTO registerPlayer(UserAuthRequestDTO request) {
        // 1. Check if the username is already taken locally before doing anything
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken in this game.");
        }

        // 2. Call Node.js Wallet API to register globally
        String registerUrl = walletApiUrl + "/api/v1/user/register"; // Adjust to match Node route

        Map<String, String> walletPayload = new HashMap<>();
        walletPayload.put("email", request.getEmail());
        walletPayload.put("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", walletApiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(walletPayload, headers);

        try {
            ResponseEntity<WalletBaseResponseDTO> response = restTemplate.postForEntity(
                registerUrl, entity, WalletBaseResponseDTO.class);

            WalletBaseResponseDTO walletResponse = response.getBody();
            if (walletResponse != null && walletResponse.isSuccess()) {

                // 3. Registration successful! Extract the new global UUID
                UUID globalUserId = UUID.fromString(walletResponse.getPayload().get("global_user_id"));

                // 4. Create the local game profile
                User newUser = new User();
                newUser.setUsername(request.getUsername());
                newUser.setGlobalUserId(globalUserId);
                newUser = userRepo.save(newUser);

                return new UserAuthResponseDTO(newUser.getUserId(), globalUserId, newUser.getUsername(), newUser.getPremiumCurrency());
            } else {
                throw new RuntimeException("Wallet API failed: " + (walletResponse != null ? walletResponse.getMessage() : "Unknown error"));
            }

        } catch (HttpClientErrorException e) {
            // e.getResponseBodyAsString() contains the exact JSON your Node API sent back!
            String nodeErrorJson = e.getResponseBodyAsString();
            throw new RuntimeException("Node API Rejected: " + nodeErrorJson);
        } catch (Exception e) {
            // Catch-all for connection timeouts or other unexpected crashes
            throw new RuntimeException("Internal Server Error: " + e.getMessage());
        }
    }

    public UserAuthResponseDTO loginPlayer(UserAuthRequestDTO request) {
        // 1. Call Node.js Wallet API to verify password
        String loginUrl = walletApiUrl + "/api/v1/user/login"; // Adjust to match Node route

        Map<String, String> walletPayload = new HashMap<>();
        walletPayload.put("email", request.getEmail());
        walletPayload.put("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", walletApiKey);
        headers.set("Content-Type", "application/json");
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(walletPayload, headers);

        try {
            ResponseEntity<WalletBaseResponseDTO> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                entity,
                WalletBaseResponseDTO.class);

            WalletBaseResponseDTO walletResponse = response.getBody();
            if (walletResponse != null && walletResponse.isSuccess()) {

                // 2. Authentication successful! Extract global UUID
                UUID globalUserId = UUID.fromString(walletResponse.getPayload().get("global_user_id"));

                // 3. Find their local game profile
                Optional<User> localUserOpt = userRepo.findByGlobalUserId(globalUserId);

                if (localUserOpt.isPresent()) {
                    User localUser = localUserOpt.get();
                    return new UserAuthResponseDTO(localUser.getUserId(), globalUserId, localUser.getUsername(), localUser.getPremiumCurrency());
                } else {
                    // Edge Case: They have a global wallet, but have never played YOUR game before!
                    // In a production game, you would redirect them to a "Create Username" screen here.
                    throw new RuntimeException("Global account found, but no local game profile exists. Please register a username.");
                }
            } else {
                throw new RuntimeException("Invalid credentials.");
            }
        } catch (HttpClientErrorException e) {
            // Catches the 401 Unauthorized from Node.js
            throw new RuntimeException("Invalid email or password.");
        }
    }
}