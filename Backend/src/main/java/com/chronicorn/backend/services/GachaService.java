package com.chronicorn.backend.services;

import com.chronicorn.backend.dto.GachaResultDTO;
import com.chronicorn.backend.dto.BannerItem;
import com.chronicorn.backend.models.BaseCharacter;
import com.chronicorn.backend.models.PlayerCharacter;
import com.chronicorn.backend.models.User;
import com.chronicorn.backend.models.UserPity;
import com.chronicorn.backend.repositories.BaseCharacterRepository;
import com.chronicorn.backend.repositories.PlayerCharacterRepository;
import com.chronicorn.backend.repositories.UserPityRepository;
import com.chronicorn.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GachaService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PlayerCharacterRepository playerCharRepo;

    @Autowired
    private BaseCharacterRepository baseCharRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserPityRepository pityRepo;

    private final Random random = new Random();
    private final int PULL_COST = 160;

    // --- GACHA CONFIGURATION ---
    private final int HARD_PITY_THRESHOLD = 50;
    private final double RATE_5_STAR = 0.01; // 1%
    private final double RATE_4_STAR = 0.99; // 9% (Cumulative 0.01 to 0.10)

    private List<BannerItem> getBannerItems(String bannerId) {
        String sql = "SELECT bp.char_id as id, c.rarity as rarity, false as is_weapon " +
                "FROM banner_pool bp " +
                "JOIN base_characters c ON bp.char_id = c.char_id " +
                "WHERE bp.banner_id = ? AND bp.char_id IS NOT NULL " +
                "UNION ALL " +
                "SELECT bp.weapon_id as id, w.rarity as rarity, true as is_weapon " +
                "FROM banner_pool bp " +
                "JOIN base_weapon w ON bp.weapon_id = w.weapon_id " +
                "WHERE bp.banner_id = ? AND bp.weapon_id IS NOT NULL";

        return jdbcTemplate.query(sql, new RowMapper<BannerItem>() {
            @Override
            public BannerItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BannerItem(
                        rs.getString("id"),
                        rs.getInt("rarity"),
                        rs.getBoolean("is_weapon"));
            }
        }, bannerId, bannerId);
    }

    // Clean helper method to resolve the banner's type
    private String resolveBannerType(String bannerId) {
        String sql = "SELECT banner_type FROM gacha_banners WHERE banner_id = ?";
        try {
            String type = jdbcTemplate.queryForObject(sql, String.class, bannerId);
            return (type != null && !type.trim().isEmpty()) ? type : "standard";
        } catch (Exception e) {
            // Fallback in case the banner isn't found or missing a type
            return "standard";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<GachaResultDTO> performMultiPull(UUID userId, String bannerId, int pullCount) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int totalCost = PULL_COST * pullCount;
        if (user.getPremiumCurrency() < totalCost) {
            throw new RuntimeException("Insufficient premium currency");
        }

        user.setPremiumCurrency(user.getPremiumCurrency() - totalCost);
        userRepo.save(user);

        // --- THE FIX: Fetch Pity using banner_type ---
        String bannerType = resolveBannerType(bannerId);
        UserPity pityTracker = pityRepo.findByUserIdAndBannerType(userId, bannerType)
                .orElse(new UserPity(userId, bannerType, 0));

        // 3. Prepare the Drop Pools based on the EXACT bannerId
        List<BannerItem> allItems = getBannerItems(bannerId);

        List<BannerItem> pool5Star = allItems.stream().filter(c -> c.getRarity() == 5).collect(Collectors.toList());
        List<BannerItem> pool4Star = allItems.stream().filter(c -> c.getRarity() == 4).collect(Collectors.toList());

        if (pool4Star.isEmpty() || pool5Star.isEmpty()) {
            throw new RuntimeException(
                    "Database is missing items for one or more rarities in banner " + bannerId + "!");
        }

        List<PlayerCharacter> ownedCharList = playerCharRepo.findAllByUserId(userId);
        Map<String, PlayerCharacter> ownedCharMap = new HashMap<>();
        for (PlayerCharacter pc : ownedCharList) {
            ownedCharMap.put(pc.getCharId(), pc);
        }

        List<GachaResultDTO> results = new ArrayList<>();

        for (int i = 0; i < pullCount; i++) {
            pityTracker.incrementPity();
            int currentPity = pityTracker.getPityCount();
            List<BannerItem> winningPool;

            if (currentPity >= HARD_PITY_THRESHOLD) {
                winningPool = pool5Star;
                pityTracker.resetPity();
            } else {
                double roll = Math.random();
                if (roll < RATE_5_STAR) {
                    winningPool = pool5Star;
                    pityTracker.resetPity();
                } else {
                    winningPool = pool4Star;
                }
            }

            BannerItem pulledItem = winningPool.get(random.nextInt(winningPool.size()));
            String pulledItemId = pulledItem.getId();
            boolean isNew = false;

            if (pulledItem.isWeapon()) {
                isNew = true;
            } else {
                PlayerCharacter pc = ownedCharMap.get(pulledItemId);
                if (pc != null) {
                    pc.setDupesCount(pc.getDupesCount() + 1);
                    playerCharRepo.save(pc);
                } else {
                    pc = new PlayerCharacter(userId, pulledItemId);
                    playerCharRepo.save(pc);
                    ownedCharMap.put(pulledItemId, pc);
                    isNew = true;
                }
            }
            results.add(new GachaResultDTO(pulledItemId, isNew));
        }

        pityRepo.save(pityTracker);
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    public GachaResultDTO performPull(UUID userId, String bannerId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPremiumCurrency() < PULL_COST) {
            throw new RuntimeException("Insufficient premium currency");
        }

        user.setPremiumCurrency(user.getPremiumCurrency() - PULL_COST);
        userRepo.save(user);

        // --- THE FIX: Fetch Pity using banner_type ---
        String bannerType = resolveBannerType(bannerId);
        UserPity pityTracker = pityRepo.findByUserIdAndBannerType(userId, bannerType)
                .orElse(new UserPity(userId, bannerType, 0));

        List<BannerItem> allItems = getBannerItems(bannerId);
        List<BannerItem> pool5Star = allItems.stream().filter(c -> c.getRarity() == 5).collect(Collectors.toList());
        List<BannerItem> pool4Star = allItems.stream().filter(c -> c.getRarity() == 4).collect(Collectors.toList());

        if (pool4Star.isEmpty() || pool5Star.isEmpty()) {
            throw new RuntimeException("No items available in the gacha pool for banner " + bannerId);
        }

        pityTracker.incrementPity();
        int currentPity = pityTracker.getPityCount();
        List<BannerItem> winningPool;

        if (currentPity >= HARD_PITY_THRESHOLD) {
            winningPool = pool5Star;
            pityTracker.resetPity();
        } else {
            double roll = Math.random();
            if (roll < RATE_5_STAR) {
                winningPool = pool5Star;
                pityTracker.resetPity();
            } else {
                winningPool = pool4Star;
            }
        }

        BannerItem pulledItem = winningPool.get(random.nextInt(winningPool.size()));
        String pulledItemId = pulledItem.getId();
        boolean isNew = false;

        if (pulledItem.isWeapon()) {
            isNew = true;
        } else {
            Optional<PlayerCharacter> existingCharacter = playerCharRepo.findByUserIdAndCharId(userId, pulledItemId);
            if (existingCharacter.isPresent()) {
                PlayerCharacter pc = existingCharacter.get();
                pc.setDupesCount(pc.getDupesCount() + 1);
                playerCharRepo.save(pc);
            } else {
                PlayerCharacter newCharacter = new PlayerCharacter(userId, pulledItemId);
                playerCharRepo.save(newCharacter);
                isNew = true;
            }
        }

        pityRepo.save(pityTracker);
        return new GachaResultDTO(pulledItemId, isNew);
    }

    @Transactional
    public GachaResultDTO grantSpecificCharacter(UUID userId, String specificCharId) {
        Optional<PlayerCharacter> existing = playerCharRepo.findByUserIdAndCharId(userId, specificCharId);
        boolean isNew = false;

        if (existing.isPresent()) {
            PlayerCharacter pc = existing.get();
            pc.setDupesCount(pc.getDupesCount() + 1);
            playerCharRepo.save(pc);
        } else {
            PlayerCharacter newChar = new PlayerCharacter(userId, specificCharId);
            playerCharRepo.save(newChar);
            isNew = true;
        }

        return new GachaResultDTO(specificCharId, isNew);
    }
}