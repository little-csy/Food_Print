package com.foodprint.user.service;

import com.foodprint.user.dto.BodyDataRequest;
import com.foodprint.user.dto.UserProfileRequest;
import com.foodprint.user.entity.User;
import com.foodprint.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public User getOrCreateUser(String userId, String email) {
        UUID uuid = UUID.fromString(userId);
        return userRepository.findById(uuid).orElseGet(() -> {
            User user = new User();
            user.setId(uuid);
            user.setEmail(email);
            return userRepository.save(user);
        });
    }

    public User updateProfile(String userId, UserProfileRequest request) {
        User user = getUserById(userId);
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        return userRepository.save(user);
    }

    public User updateBodyData(String userId, BodyDataRequest request) {
        User user = getUserById(userId);
        if (request.getHeightCm() != null) user.setHeightCm(request.getHeightCm());
        if (request.getWeightKg() != null) user.setWeightKg(request.getWeightKg());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getGoal() != null) user.setGoal(request.getGoal());
        if (request.getDailyCalorieTarget() != null) user.setDailyCalorieTarget(request.getDailyCalorieTarget());
        if (request.getDietaryPreference() != null) user.setDietaryPreference(request.getDietaryPreference());
        return userRepository.save(user);
    }
}
