package com.foodprint.ai.service;

import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.SaveAIAnalysisLogRequest;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.entity.NutritionData;
import com.foodprint.food.repository.FoodLogRepository;
import com.foodprint.food.repository.NutritionDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AIMealLogService {

    private final FoodLogRepository foodLogRepository;
    private final NutritionDataRepository nutritionDataRepository;

    public AIMealLogService(FoodLogRepository foodLogRepository, NutritionDataRepository nutritionDataRepository) {
        this.foodLogRepository = foodLogRepository;
        this.nutritionDataRepository = nutritionDataRepository;
    }

    /**
     * Persists an AI analysis result into the global food_logs and nutrition_details tables.
     */
    @Transactional
    public FoodLog saveAnalysisLog(UUID userId, SaveAIAnalysisLogRequest request) {
        FoodAnalysisResult analysis = request.getAnalysisResult();
        if (analysis == null) {
            throw new IllegalArgumentException("AI analysis result is missing in the request.");
        }

        // 1. Create primary FoodLog entry
        FoodLog foodLog = new FoodLog();
        foodLog.setUserId(userId);
        foodLog.setTitle(request.getTitle());
        foodLog.setImageUrl(request.getImageUrl());
        
        LocalDateTime mealTime = request.getMealTime() != null ? request.getMealTime() : LocalDateTime.now();
        foodLog.setMealTime(mealTime);
        foodLog.setLogDate(mealTime.toLocalDate());
        
        // Capture AI-specific metadata
        foodLog.setAiConfidence(analysis.getConfidence());

        FoodLog savedLog = foodLogRepository.save(foodLog);

        // 2. Persist individual food items
        if (analysis.getFoods() != null) {
            for (FoodAnalysisResult.FoodNutritionDetail detail : analysis.getFoods()) {
                NutritionData nut = new NutritionData();
                nut.setFoodLogId(savedLog.getId());
                nut.setNameEn(detail.getNameEn());
                nut.setNameZh(detail.getNameZh());
                
                // Robust Null Protection for AI metrics
                nut.setCalories(Optional.ofNullable(detail.getCalories()).orElse(0.0));
                nut.setProteinG(Optional.ofNullable(detail.getProteinG()).orElse(0.0));
                nut.setFatG(Optional.ofNullable(detail.getFatG()).orElse(0.0));
                nut.setCarbsG(Optional.ofNullable(detail.getCarbsG()).orElse(0.0));
                nut.setFiberG(Optional.ofNullable(detail.getFiberG()).orElse(0.0));
                
                nut.setPortionAmount(detail.getPortionAmount());
                nut.setPortionUnit(detail.getPortionUnit());
                nut.setDataSource(detail.getDataSource());
                
                nutritionDataRepository.save(nut);
            }
        }

        return savedLog;
    }
}
