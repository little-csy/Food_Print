package com.foodprint.user.controller;

import com.foodprint.user.dto.BodyDataRequest;
import com.foodprint.user.dto.UserProfileRequest;
import com.foodprint.user.dto.UserStatsResponse;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import com.foodprint.food.repository.FoodLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FoodLogRepository foodLogRepository;

    public UserController(UserService userService, FoodLogRepository foodLogRepository) {
        this.userService = userService;
        this.foodLogRepository = foodLogRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMe(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        User user = userService.getOrCreateUser(userId, email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal Jwt jwt,
            @RequestBody UserProfileRequest request) {
        User user = userService.updateProfile(jwt.getSubject(), request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me/body-data")
    public ResponseEntity<User> updateBodyData(@AuthenticationPrincipal Jwt jwt,
            @RequestBody BodyDataRequest request) {
        User user = userService.updateBodyData(jwt.getSubject(), request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getMyStats(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        long daysActive = foodLogRepository.countDistinctLogDateByUserId(java.util.UUID.fromString(userId));

        // Mock achievements and goals count as the challenge module is not yet complete
        int mockAchievements = 24;
        int mockGoalsMet = 18;

        UserStatsResponse response = new UserStatsResponse(mockAchievements, mockGoalsMet, daysActive);
        return ResponseEntity.ok(response);
    }
}
