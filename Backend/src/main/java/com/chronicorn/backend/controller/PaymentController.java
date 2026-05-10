package com.chronicorn.backend.controller;

import com.chronicorn.backend.dto.PurchaseRequestDTO;
import com.chronicorn.backend.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // POST: http://localhost:8080/api/payment/buy-currency
    @PostMapping("/buy-currency")
    public ResponseEntity<String> buyCurrency(@RequestBody PurchaseRequestDTO request) {
        try {
            boolean success = paymentService.buyPremiumCurrency(
                    request.getLocalUserId(),
                    request.getCost(),
                    request.getCurrencyAmount()
            );

            if (success) {
                return ResponseEntity.ok("Purchase successful. Currency added.");
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Purchase failed. Insufficient funds in Central Wallet.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}