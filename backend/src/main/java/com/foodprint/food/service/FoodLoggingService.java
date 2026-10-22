package com.foodprint.food.service;

// TODO [Engineer B]: Implement FoodLoggingService
//
// This is the key integration point with the AI module.
// Responsibilities:
// 1. Build UserNutritionContext from user profile
// 2. Call AIRequestRouter for food analysis
// 3. Save FoodLog and NutritionData into database
// 4. Return saved FoodLog to controller

import com.foodprint.ai.dto.*;
import com.foodprint.ai.service.AIRequestRouter;
import com.foodprint.food.dto.*;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.entity.NutritionData;
import com.foodprint.food.repository.FoodLogRepository;
import com.foodprint.food.repository.NutritionDataRepository;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import com.foodprint.util.Constants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FoodLoggingService {

    private final FoodLogRepository foodLogRepository;
    private final NutritionDataRepository nutritionDataRepository;
    private final UserService userService;
    private final AIRequestRouter aiRouter;

    public FoodLoggingService(
            FoodLogRepository foodLogRepository,
            NutritionDataRepository nutritionDataRepository,
            UserService userService,
            AIRequestRouter aiRouter) {
        this.foodLogRepository = foodLogRepository;
        this.nutritionDataRepository = nutritionDataRepository;
        this.userService = userService;
        this.aiRouter = aiRouter;
    }

    private Double getSafeValue(Double value) {
        return value != null ? value : 0.0;
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return UUID.fromString(authentication.getName());
    }

    // =========================================================================
    // Home Screen Features
    // =========================================================================

    /**
     * Returns a list of all meals logged today with their total calorie counts
     * and a list of food item names. Sorted by time descending.
     */
    public List<TodayMealResponse> getTodayMeals() {
        UUID userId = getCurrentUserId();
        LocalDate today = LocalDate.now();

        List<FoodLog> todayLogs = foodLogRepository.findByUserIdAndLogDate(userId, today);

        return todayLogs.stream()
                .map(log -> {
                    List<NutritionData> items = nutritionDataRepository.findByFoodLogId(log.getId());

                    // Aggregate total calories
                    double totalCals = items.stream()
                            .mapToDouble(n -> n.getCalories() != null ? n.getCalories() : 0.0)
                            .sum();

                    // Collect all food names
                    List<String> foodNames = items.stream()
                            .map(n -> n.getNameEn() != null ? n.getNameEn() : "Unknown item")
                            .collect(Collectors.toList());

                    return new TodayMealResponse(
                            log.getId(),
                            log.getTitle() != null ? log.getTitle() : "Unnamed Meal",
                            foodNames,
                            totalCals,
                            log.getMealTime() != null ? log.getMealTime() : log.getCreatedAt()
                    );
                })
                // Sort by time descending (latest first)
                .sorted((a, b) -> b.getMealTime().compareTo(a.getMealTime()))
                .collect(Collectors.toList());
    }

    @Transactional
    public FoodLog saveManualMealLog(ManualMealLogRequest request) {
        UUID userId = getCurrentUserId();
        FoodLog foodLog = new FoodLog();
        foodLog.setUserId(userId);
        foodLog.setTitle(request.getTitle());
        foodLog.setImageUrl(request.getImageUrl());
        
        LocalDateTime mealTime = request.getMealTime() != null ? request.getMealTime() : LocalDateTime.now();
        foodLog.setMealTime(mealTime);
        foodLog.setLogDate(mealTime.toLocalDate());
        
        FoodLog savedLog = foodLogRepository.save(foodLog);

        if (request.getFoods() != null) {
            for (MealItemDTO item : request.getFoods()) {
                NutritionData nut = new NutritionData();
                nut.setFoodLogId(savedLog.getId());
                nut.setNameEn(item.getNameEn());
                nut.setNameZh(item.getNameZh());
                
                // Robust Null Protection: Default to 0.0 if AI or User misses a field
                nut.setCalories(Optional.ofNullable(item.getCalories()).orElse(0.0));
                nut.setProteinG(Optional.ofNullable(item.getProteinG()).orElse(0.0));
                nut.setFatG(Optional.ofNullable(item.getFatG()).orElse(0.0));
                nut.setCarbsG(Optional.ofNullable(item.getCarbsG()).orElse(0.0));
                nut.setFiberG(Optional.ofNullable(item.getFiberG()).orElse(0.0));
                
                nut.setPortionAmount(item.getPortionAmount());
                nut.setPortionUnit(item.getPortionUnit());
                nut.setDataSource(item.getDataSource());
                
                nutritionDataRepository.save(nut);
            }
        }
        return savedLog;
    }

    @Transactional
    public FoodLog createFoodLog(
            String text,
            String imageUrl,
            LocalDate date) {
        UUID userId = getCurrentUserId();
        // At least one input is required
        if ((text == null || text.isBlank()) && (imageUrl == null || imageUrl.isBlank())) {
            throw new IllegalArgumentException("Either text or imageUrl must be provided.");
        }

        // Load user profile and build AI context
        User user = userService.getUserById(userId.toString());

        UserNutritionContext context = new UserNutritionContext();
        context.setHeightCm(user.getHeightCm());
        context.setWeightKg(user.getWeightKg());
        context.setAge(user.getAge());
        context.setGender(user.getGender());
        context.setGoal(user.getGoal());
        context.setDailyCalorieTarget(
                user.getDailyCalorieTarget() != null ? user.getDailyCalorieTarget().intValue() : null);

        // Placeholder values for today's intake summary
        context.setTodayCalories(0.0);
        context.setTodayProtein(0.0);
        context.setTodayFat(0.0);
        context.setTodayCarbs(0.0);

        // Call AI module
        AIResponse response;
        if (imageUrl != null && !imageUrl.isBlank()) {
            response = aiRouter.routeImageUrl(imageUrl);
        } else {
            response = aiRouter.routeText(text, context);
        }

        if (response == null || !"FOOD_ANALYSIS".equals(response.getType())) {
            throw new RuntimeException("AI did not return FOOD_ANALYSIS result.");
        }

        FoodAnalysisResult analysis = response.getFoodAnalysis();
        if (analysis == null) {
            throw new RuntimeException("Food analysis result is missing.");
        }

        // Save main food log
        FoodLog foodLog = new FoodLog();
        foodLog.setUserId(userId);
        foodLog.setLogDate(date != null ? date : LocalDate.now());
        foodLog.setMealTime(LocalDateTime.now());
        foodLog.setImageUrl(imageUrl);
        foodLog.setOriginalText(text);
        foodLog.setAiConfidence(analysis.getConfidence());

        FoodLog savedLog = foodLogRepository.save(foodLog);

        // Save per-food-item nutrition data
        if (analysis.getFoods() != null) {
            for (FoodAnalysisResult.FoodNutritionDetail item : analysis.getFoods()) {
                NutritionData nutritionData = new NutritionData();
                nutritionData.setFoodLogId(savedLog.getId());
                nutritionData.setNameEn(item.getNameEn());
                nutritionData.setNameZh(item.getNameZh());
                
                // Null Protection for AI results
                nutritionData.setCalories(Optional.ofNullable(item.getCalories()).orElse(0.0));
                nutritionData.setProteinG(Optional.ofNullable(item.getProteinG()).orElse(0.0));
                nutritionData.setFatG(Optional.ofNullable(item.getFatG()).orElse(0.0));
                nutritionData.setCarbsG(Optional.ofNullable(item.getCarbsG()).orElse(0.0));
                nutritionData.setFiberG(Optional.ofNullable(item.getFiberG()).orElse(0.0));
                
                nutritionData.setPortionAmount(item.getPortionAmount());
                nutritionData.setPortionUnit(item.getPortionUnit());
                nutritionData.setDataSource(item.getDataSource());

                nutritionDataRepository.save(nutritionData);
            }
        }

        return savedLog;
    }

    public WeeklyOverviewResponse getWeeklyOverview(LocalDate endDate) {
        UUID userId = getCurrentUserId();
        LocalDate startDate = endDate.minusDays(6);
        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDateBetween(userId, startDate, endDate);

        // Pre-fetch all nutrition data to avoid N+1 and aggregate by day
        List<DailyNutrition> days = new ArrayList<>();
        double totalWeekCal = 0.0;
        
        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            
            // Filter logs for this specific day
            List<FoodLog> dailyLogs = logs.stream()
                    .filter(log -> log.getLogDate().equals(d))
                    .collect(Collectors.toList());
            
            double dCal = 0, dProt = 0, dFat = 0, dCarb = 0;
            
            for (FoodLog log : dailyLogs) {
                List<NutritionData> items = nutritionDataRepository.findByFoodLogId(log.getId());
                for (NutritionData nut : items) {
                    dCal += Optional.ofNullable(nut.getCalories()).orElse(0.0);
                    dProt += Optional.ofNullable(nut.getProteinG()).orElse(0.0);
                    dFat += Optional.ofNullable(nut.getFatG()).orElse(0.0);
                    dCarb += Optional.ofNullable(nut.getCarbsG()).orElse(0.0);
                }
            }
            
            totalWeekCal += dCal;
            days.add(new DailyNutrition(d, dCal, dProt, dFat, dCarb));
        }

        double average = Math.round((totalWeekCal / 7.0) * 10.0) / 10.0;
        return new WeeklyOverviewResponse(days, average);
    }
}
