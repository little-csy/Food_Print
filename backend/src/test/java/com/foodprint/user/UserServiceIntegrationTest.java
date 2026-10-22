package com.foodprint.user;

import com.foodprint.user.dto.BodyDataRequest;
import com.foodprint.user.dto.UserProfileRequest;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test - connects to real Supabase database.
 * Run with: set -a && source .env && set +a && JAVA_HOME=$(/usr/libexec/java_home -v 21) mvn test -Dtest=UserServiceIntegrationTest -Dspring.profiles.active=prod
 */
@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    // UUID of a test user created with Supabase Auth
    private static final String TEST_USER_ID = "31855e32-8204-4873-a2cd-76598eab12b8";
    private static final String TEST_EMAIL = "test@foodprint.dev";

    @Test
    void testCreateAndGetUser() {
        // Create or retrieve the user (writes to the profiles table)
        User user = userService.getOrCreateUser(TEST_USER_ID, TEST_EMAIL);

        assertNotNull(user);
        assertEquals(TEST_USER_ID, user.getId().toString());
        assertEquals(TEST_EMAIL, user.getEmail());

        System.out.println("✅ User created/retrieved: " + user.getId() + " - " + user.getEmail());
    }

    @Test
    void testUpdateBodyData() {
        // Ensure the user exists first
        userService.getOrCreateUser(TEST_USER_ID, TEST_EMAIL);

        // Update body measurements
        BodyDataRequest request = new BodyDataRequest();
        request.setHeightCm(175.0);
        request.setWeightKg(70.0);
        request.setAge(25);
        request.setGender("male");
        request.setGoal("MAINTAIN");
        request.setDailyCalorieTarget(2000.0);

        User updated = userService.updateBodyData(TEST_USER_ID, request);

        assertEquals(175.0, updated.getHeightCm());
        assertEquals(70.0, updated.getWeightKg());
        assertEquals(25, updated.getAge());
        assertEquals("MAINTAIN", updated.getGoal());

        System.out.println("✅ Body data updated: height=" + updated.getHeightCm() + ", weight=" + updated.getWeightKg());
    }

    @Test
    void testUpdateProfile() {
        userService.getOrCreateUser(TEST_USER_ID, TEST_EMAIL);

        UserProfileRequest request = new UserProfileRequest();
        request.setNickname("TestUser");

        User updated = userService.updateProfile(TEST_USER_ID, request);

        assertEquals("TestUser", updated.getNickname());

        System.out.println("✅ Profile updated: nickname=" + updated.getNickname());
    }
}
