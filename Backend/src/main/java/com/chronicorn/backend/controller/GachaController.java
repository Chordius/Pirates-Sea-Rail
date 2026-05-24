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
    public ResponseEntity<?> pullCharacter(@PathVariable UUID userId, @RequestParam(required = false, defaultValue = "standard") String bannerId) {
        try {
            GachaResultDTO result = gachaService.performPull(userId, bannerId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Returns 400 Bad Request if they don't have enough currency
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // POST: http://localhost:8080/api/gacha/pull10/{userId}
    @PostMapping("/pull10/{userId}")
    public ResponseEntity<?> pull10Characters(@PathVariable UUID userId, @RequestParam(required = false, defaultValue = "standard") String bannerId) {
        try {
            java.util.List<GachaResultDTO> results = gachaService.performMultiPull(userId, bannerId, 10);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
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

        return ResponseEntity.ok(ownedCount == request.getPartyCharIds().size());
    }

    @PostMapping("/grant/{userId}/{charId}")
public ResponseEntity<?> grantCharacter(@PathVariable UUID userId, @PathVariable String charId) {
    try {
        GachaResultDTO result = gachaService.grantSpecificCharacter(userId, charId);
        return ResponseEntity.ok(result);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
}