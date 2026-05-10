package com.chronicorn.backend.services;

import com.chronicorn.backend.models.User;
import com.chronicorn.backend.models.LocalOrder;
import com.chronicorn.backend.repositories.UserRepository;
import com.chronicorn.backend.repositories.OrderRepository;
import com.chronicorn.backend.dto.JigsawCoinTransactionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Value("${wallet.api.url}") // Set this in application.properties
    private String walletApiUrl;

    @Value("${wallet.api.key}") // Set this in application.properties
    private String myAppApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public boolean buyPremiumCurrency(UUID localUserId, BigDecimal cost, int currencyAmount) {

        User user = userRepo.findById(localUserId).orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Create a PENDING local order
        LocalOrder order = new LocalOrder();
        order.setUserId(localUserId);
        order.setAmountPaid(cost);
        order.setPremiumCurrencyGranted(currencyAmount);
        order.setStatus("PENDING");
        order = orderRepo.save(order);

        // 2. Prepare the request to the Central Wallet API
        // Note: Amount is negative because it is a deduction based on your schema comments
        // 1. Prepare the JSON body (No API key here anymore)
        JigsawCoinTransactionDTO requestBody = new JigsawCoinTransactionDTO(
                user.getGlobalUserId(),
                cost.negate(),
                order.getOrderId().toString()
        );

        // 2. Prepare the HTTP Headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("x-api-key", myAppApiKey); // Matches req.headers['x-api-key']
        headers.set("Content-Type", "application/json");

        // 3. Bundle them together
        org.springframework.http.HttpEntity<JigsawCoinTransactionDTO> entity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

        try {
            // 4. Use exchange() instead of postForEntity() to send headers
            ResponseEntity<String> response = restTemplate.exchange(
                    walletApiUrl + "/api/v1/wallet/transaction",
                    org.springframework.http.HttpMethod.POST,
                    entity,
                    String.class
            );

            // 4. Evaluate the Wallet API response
            if (response.getStatusCode().is2xxSuccessful()) {
                // Success: Update order status
                order.setStatus("COMPLETED");
                orderRepo.save(order);

                // Grant the local premium currency for the gacha pulls
                user.setPremiumCurrency(user.getPremiumCurrency() + currencyAmount);
                userRepo.save(user);

                return true;
            } else {
                // Wallet API rejected the transaction (e.g., insufficient funds)
                order.setStatus("FAILED");
                orderRepo.save(order);
                return false;
            }

        } catch (Exception e) {
            // Network failure or API down
            order.setStatus("ERROR");
            orderRepo.save(order);
            throw new RuntimeException("Communication with Central Wallet failed.");
        }
    }
}