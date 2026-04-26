package com.chronicorn.backend.repository;

import com.chronicorn.backend.model.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<PlayerEntity, Long>{
    // Mencari player berdasarkan username
    Optional<PlayerEntity> findByUsername(String username);

    // Mengambil Top 10 Highscore (waktu > 0)
    List<PlayerEntity> findTop10ByScoreGreaterThanOrderByScoreAsc(int minScore);
}
