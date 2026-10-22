package com.foodprint.ai.service;

import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.SaveAIAnalysisLogRequest;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.entity.NutritionData;
import com.foodprint.food.repository.FoodLogRepository;
import com.foodprint.food.repository.NutritionDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AIMealLogServiceTest {

    @Mock
    private FoodLogRepository foodLogRepository;

    @Mock
    private NutritionDataRepository nutritionDataRepository;

    @InjectMocks
    private AIMealLogService aiMealLogService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    @Test
    void testSaveAnalysisLog_FullSuccess() {
        // Arrange
        SaveAIAnalysisLogRequest request = new SaveAIAnalysisLogRequest();
        request.setTitle("AI Lunch");
        request.setMealTime(LocalDateTime.now());
        request.setImageUrl("http://example.com/food.jpg");

        FoodAnalysisResult analysis = new FoodAnalysisResult();
        analysis.setConfidence(0.95);
        
        FoodAnalysisResult.FoodNutritionDetail foodDetail = new FoodAnalysisResult.FoodNutritionDetail();
        foodDetail.setNameEn("Burger");
        foodDetail.setCalories(800.0);
        foodDetail.setProteinG(30.0);
        // Leaving Fat/Carbs as 0 or null internally
        analysis.setFoods(List.of(foodDetail));
        
        request.setAnalysisResult(analysis);

        when(foodLogRepository.save(any(FoodLog.class))).thenAnswer(invocation -> {
            FoodLog log = invocation.getArgument(0);
            if (log.getId() == null) {
                log.setId(UUID.randomUUID());
            }
            return log;
        });

        // Act
        FoodLog result = aiMealLogService.saveAnalysisLog(userId, request);

        // Assert
        assertNotNull(result);
        verify(foodLogRepository, times(1)).save(any(FoodLog.class));
        verify(nutritionDataRepository, times(1)).save(any(NutritionData.class));
        
        assertEquals(0.95, result.getAiConfidence());
    }

    @Test
    void testSaveAnalysisLog_NullNutritionSafety() {
        // Arrange
        SaveAIAnalysisLogRequest request = new SaveAIAnalysisLogRequest();
        FoodAnalysisResult analysis = new FoodAnalysisResult();
        
        // Item with missing calories
        FoodAnalysisResult.FoodNutritionDetail foodDetail = new FoodAnalysisResult.FoodNutritionDetail();
        foodDetail.setNameEn("Mystery Food");
        // No calories set
        
        analysis.setFoods(List.of(foodDetail));
        request.setAnalysisResult(analysis);

        when(foodLogRepository.save(any(FoodLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() -> aiMealLogService.saveAnalysisLog(userId, request));
        verify(nutritionDataRepository).save(argThat(nut -> nut.getCalories() == 0.0));
    }
}
