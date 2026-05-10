package com.chronicorn.backend.controller;

import com.chronicorn.backend.dto.GachaResultDTO;
import com.chronicorn.backend.dto.PartyRequestDTO;
import com.chronicorn.backend.services.GachaService;
import com.chronicorn.backend.repositories.PlayerCharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gacha")
public class GachaController {

    @Autowired
    private GachaService gachaService;

    @Autowired
    private PlayerCharacterRepository playerCharRepo;

    // POST: http://localhost:8080/api/gacha/pull/{userId}
    @PostMapping("/pull/{userId}")
    public ResponseEntity<?> pullCharacter(@PathVariable UUID userId) {
        try {
            GachaResultDTO result = gachaService.performPull(userId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Returns 400 Bad Request if they don't have enough currency
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/gacha/verify
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyParty(@RequestBody PartyRequestDTO request) {

        int ownedCount = playerCharRepo.countByUserIdAndCharIdIn(
                request.getUserId(),
                request.getPartyCharIds()
        );

        if (ownedCount == request.getPartyCharIds().size()) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(false);
        }
    }
}