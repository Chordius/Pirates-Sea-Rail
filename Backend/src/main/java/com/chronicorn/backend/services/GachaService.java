package com.chronicorn.backend.services;

import com.chronicorn.backend.dto.GachaResultDTO;
import com.chronicorn.backend.models.BaseCharacter;
import com.chronicorn.backend.models.PlayerCharacter;
import com.chronicorn.backend.models.User;
import com.chronicorn.backend.repositories.BaseCharacterRepository;
import com.chronicorn.backend.repositories.PlayerCharacterRepository;
import com.chronicorn.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class GachaService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PlayerCharacterRepository playerCharRepo;

    @Autowired
    private BaseCharacterRepository baseCharRepo;

    private final Random random = new Random();
    private final int PULL_COST = 160;

    @Transactional
    public GachaResultDTO performPull(UUID userId) {

        // 1. Verify user and currency
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPremiumCurrency() < PULL_COST) {
            throw new RuntimeException("Insufficient premium currency");
        }

        // 2. Deduct currency
        user.setPremiumCurrency(user.getPremiumCurrency() - PULL_COST);
        userRepo.save(user);

        // 3. Fetch all possible characters from the database to build the drop pool
        List<BaseCharacter> availableCharacters = baseCharRepo.findAll();
        if (availableCharacters.isEmpty()) {
            throw new RuntimeException("No characters available in the gacha pool");
        }

        // 4. RNG Selection (A basic random selection for this implementation)
        int randomIndex = random.nextInt(availableCharacters.size());
        BaseCharacter pulledCharacter = availableCharacters.get(randomIndex);
        String pulledCharId = pulledCharacter.getCharId();

        // 5. Check if the user already owns this character
        Optional<PlayerCharacter> existingCharacter = playerCharRepo.findByUserIdAndCharId(userId, pulledCharId);

        boolean isNew = false;
        if (existingCharacter.isPresent()) {
            // Character exists: Increment dupes count
            PlayerCharacter characterToUpdate = existingCharacter.get();
            characterToUpdate.setDupesCount(characterToUpdate.getDupesCount() + 1);
            playerCharRepo.save(characterToUpdate);
        } else {
            // Character is new: Create a new row
            PlayerCharacter newCharacter = new PlayerCharacter(userId, pulledCharId);
            playerCharRepo.save(newCharacter);
            isNew = true;
        }

        // 6. Return the result to the Controller
        return new GachaResultDTO(pulledCharId, isNew);
    }
}