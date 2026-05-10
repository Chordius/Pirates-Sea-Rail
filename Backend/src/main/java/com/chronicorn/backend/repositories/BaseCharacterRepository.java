package com.chronicorn.backend.repositories;

import com.chronicorn.backend.models.BaseCharacter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseCharacterRepository extends JpaRepository<BaseCharacter, String> {
}