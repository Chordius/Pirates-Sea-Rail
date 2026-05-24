package com.chronicorn.backend;

import com.chronicorn.backend.models.User;
import com.chronicorn.backend.repositories.UserRepository;
import com.chronicorn.backend.services.GachaService;
import com.chronicorn.backend.dto.GachaResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GachaTest {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private GachaService gachaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final UUID TEST_USER_ID = UUID.fromString("b97ff522-85ba-4631-b35c-eeda1deb0a83");

    @Test
    public void testGachaSystem() {
        System.out.println("=== Starting Gacha System Integration Test ===");

        // 1. Verify connection and print existing banners in the database
        System.out.println("Fetching banners from database...");
        List<Map<String, Object>> banners = jdbcTemplate.queryForList("SELECT * FROM gacha_banners");
        System.out.println("Available banners in database:");
        for (Map<String, Object> banner : banners) {
            System.out.println(" - " + banner.get("banner_id") + " (" + banner.get("banner_type") + ")");
        }
        assertFalse(banners.isEmpty(), "No banners found in the database. Ensure db is initialized!");

        // Use the first banner found as the test banner
        String testBannerId = (String) banners.get(0).get("banner_id");
        System.out.println("Using test banner ID: " + testBannerId);

        // 2. Check or create the test user
        User user = userRepo.findById(TEST_USER_ID).orElse(null);
        if (user == null) {
            System.out.println("Test user not found in database. Creating local test user...");
            user = new User();
            user.setUserId(TEST_USER_ID);
            user.setGlobalUserId(UUID.randomUUID());
            user.setUsername("TestGachaUser");
            user.setPremiumCurrency(10000); // Give plenty of currency
            user = userRepo.save(user);
        } else {
            System.out.println("Test user found! Username: " + user.getUsername() + ", Current Premium Currency: " + user.getPremiumCurrency());
            // Ensure they have enough currency to run tests
            if (user.getPremiumCurrency() < 2000) {
                System.out.println("Giving user additional premium currency for the test.");
                user.setPremiumCurrency(user.getPremiumCurrency() + 5000);
                user = userRepo.save(user);
            }
        }
        assertNotNull(user);

        // 3. Perform a single pull
        System.out.println("Performing a 1x pull...");
        int initialCurrency = user.getPremiumCurrency();
        GachaResultDTO singleResult = gachaService.performPull(TEST_USER_ID, testBannerId);
        System.out.println("1x Pull Result: Pulled Character ID = " + singleResult.getPulledCharId() + ", Is New = " + singleResult.getIsNew());
        assertNotNull(singleResult.getPulledCharId());

        // Verify currency deduction (1x pull cost is 160)
        User userAfter1x = userRepo.findById(TEST_USER_ID).orElseThrow();
        assertEquals(initialCurrency - 160, userAfter1x.getPremiumCurrency(), "Currency was not correctly deducted for 1x pull");

        // 4. Perform a 10x pull
        System.out.println("Performing a 10x pull...");
        List<GachaResultDTO> multiResults = gachaService.performMultiPull(TEST_USER_ID, testBannerId, 10);
        System.out.println("10x Pull Results:");
        for (int i = 0; i < multiResults.size(); i++) {
            GachaResultDTO res = multiResults.get(i);
            System.out.println(" Pull #" + (i + 1) + ": Character ID = " + res.getPulledCharId() + ", Is New = " + res.getIsNew());
        }
        assertEquals(10, multiResults.size());

        // Verify currency deduction (10x pull cost is 1600)
        User userAfter10x = userRepo.findById(TEST_USER_ID).orElseThrow();
        assertEquals(userAfter1x.getPremiumCurrency() - 1600, userAfter10x.getPremiumCurrency(), "Currency was not correctly deducted for 10x pull");

        System.out.println("=== Gacha System Integration Test Completed Successfully ===");
    }
}
