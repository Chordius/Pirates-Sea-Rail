package com.chronicorn.backend.controller;

import com.chronicorn.backend.dto.UserAuthRequestDTO;
import com.chronicorn.backend.dto.UserAuthResponseDTO;
import com.chronicorn.backend.services.UserService;
import com.chronicorn.backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;
import com.chronicorn.backend.utils.SecurityUtils;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // POST: http://localhost:8080/api/users/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserAuthRequestDTO request) {
        try {
            UserAuthResponseDTO response = userService.registerPlayer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/users/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthRequestDTO request) {
        try {
            UserAuthResponseDTO response = userService.loginPlayer(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // GET: http://localhost:8080/api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                UserAuthResponseDTO dto = new UserAuthResponseDTO(
                    user.getUserId(),
                    user.getGlobalUserId(),
                    user.getUsername(),
                    user.getPremiumCurrency()
                );
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/users/{userId}/grant-currency
    @PostMapping("/{userId}/grant-currency")
    public ResponseEntity<?> grantCurrency(
            @PathVariable UUID userId,
            @RequestParam int amount,
            @RequestParam String key) {
        try {
            String expectedKey = SecurityUtils.generateVerificationKey(userId.toString(), amount);
            if (!expectedKey.equalsIgnoreCase(key)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid verification key.");
            }

            UserAuthResponseDTO response = userService.grantPremiumCurrency(userId, amount);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}