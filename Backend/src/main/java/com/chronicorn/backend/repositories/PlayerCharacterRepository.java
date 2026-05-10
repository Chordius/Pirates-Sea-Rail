package com.chronicorn.backend.repositories;

import com.chronicorn.backend.models.PlayerCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerCharacterRepository extends JpaRepository<PlayerCharacter, UUID> {

    // Used during Gacha pulls to check if the user already owns the character
    Optional<PlayerCharacter> findByUserIdAndCharId(UUID userId, String charId);

    // Fetches the entire roster when the player logs in
    List<PlayerCharacter> findAllByUserId(UUID userId);

    // Used for the integrity check when entering a battle
    int countByUserIdAndCharIdIn(UUID userId, List<String> charIds);
}
