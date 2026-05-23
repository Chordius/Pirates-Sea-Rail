package com.chronicorn.backend.repositories;

import com.chronicorn.backend.models.UserPity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPityRepository extends JpaRepository<UserPity, UUID> {
    // Explicitly query by bannerType now
    Optional<UserPity> findByUserIdAndBannerType(UUID userId, String bannerType);
}