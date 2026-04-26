package com.chronicorn.backend.controller;

import com.chronicorn.backend.model.PlayerEntity;
import com.chronicorn.backend.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class GameController {

    @Autowired
    private PlayerRepository playerRepository;

    // Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (playerRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username sudah dipakai!");
        }

        PlayerEntity newPlayer = new PlayerEntity(username, password);
        playerRepository.save(newPlayer);
        return ResponseEntity.ok("Registrasi berhasil!");
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<PlayerEntity> playerOpt = playerRepository.findByUsername(username);

        if (playerOpt.isPresent() && playerOpt.get().getPassword().equals(password)) {
            // Return data player lengkap biar Frontend bisa set HP/ATK/Score
            return ResponseEntity.ok(playerOpt.get());
        }
        return ResponseEntity.status(401).body("Username atau Password salah");
    }

    // Update Score (Save Game)
    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody PlayerEntity updatedData) {
        Optional<PlayerEntity> playerOpt = playerRepository.findByUsername(updatedData.getUsername());

        if (playerOpt.isPresent()) {
            PlayerEntity player = playerOpt.get();
            // Update stats
            player.setHp(updatedData.getHp());
            player.setAtk(updatedData.getAtk());
            player.setDef(updatedData.getDef());

            // SPEEDRUN
            int oldScore = player.getScore();
            int newScore = updatedData.getScore();

            // Jika skor baru > 0, DAN (Skor lama 0 ATAU Skor baru < Skor lama)
            if (newScore > 0 && (oldScore == 0 || newScore < oldScore)) {
                player.setScore(newScore);
            }
            // Jika newScore 0 atau lebih lambat, jangan di-update.

            playerRepository.save(player);
            return ResponseEntity.ok("Progress tersimpan!");
        }
        return ResponseEntity.badRequest().body("Player tidak ditemukan");
    }

    // Leaderboard
    @GetMapping("/leaderboard")
    public List<PlayerEntity> getLeaderboard() {
        return playerRepository.findTop10ByScoreGreaterThanOrderByScoreAsc(0);
    }

    // Get Players
    @GetMapping("/players")
    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }
}