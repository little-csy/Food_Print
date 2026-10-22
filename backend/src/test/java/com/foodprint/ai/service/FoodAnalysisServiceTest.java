package com.foodprint.ai.service;

import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.FoodAnalysisResult.FoodNutritionDetail;
import com.foodprint.ai.dto.FoodAnalysisResult.MealSummary;
import com.foodprint.ai.dto.FoodIdentificationResult;
import com.foodprint.ai.dto.FoodIdentificationResult.FoodItem;
import com.foodprint.ai.dto.NutritionData;
import com.foodprint.ai.exception.AIServiceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Isolated tests for FoodAnalysisService.
 * Mocks GeminiService, NutritionLookupService, and NutritionPostProcessor.
 */
@ExtendWith(MockitoExtension.class)
class FoodAnalysisServiceTest {

    @Mock
    private GeminiService geminiService;
    @Mock
    private NutritionLookupService nutritionLookupService;
    @Mock
    private NutritionPostProcessor postProcessor;

    @InjectMocks
    private FoodAnalysisService foodAnalysisService;

    private FoodItem createFoodItem(String name, double portion) {
        FoodItem item = new FoodItem();
        item.setNameEn(name);
        item.setNameZh("Food name");
        item.setPortionAmount(portion);
        item.setPortionUnit("g");
        item.setEstimatedCalories(100);
        item.setEstimatedProteinG(10);
        item.setEstimatedFatG(5);
        item.setEstimatedCarbsG(10);
        item.setEstimatedFiberG(2);
        return item;
    }

    @Test
    @DisplayName("Single food from image - complete pipeline")
    void analyzeFromImage_singleFood() {
        FoodItem chicken = createFoodItem("chicken breast", 150);
        FoodIdentificationResult identified = new FoodIdentificationResult(
                List.of(chicken), "grilled chicken", 0.9);

        when(geminiService.identifyFoodFromImage(any(), anyString())).thenReturn(identified);
        when(nutritionLookupService.lookup(any())).thenReturn(
                NutritionData.fromUSDA(247.5, 46.5, 5.4, 0, 0));
        doNothing().when(postProcessor).validate(any());

        FoodAnalysisResult result = foodAnalysisService.analyzeFromImage(new byte[] { 1 }, "image/jpeg");

        assertNotNull(result);
        assertEquals(1, result.getFoods().size());
        assertEquals("chicken breast", result.getFoods().get(0).getNameEn());
        assertEquals("USDA", result.getFoods().get(0).getDataSource());
        assertEquals(247.5, result.getSummary().getTotalCalories());
        assertEquals(0.9, result.getConfidence());
    }

    @Test
    @DisplayName("Multiple foods from text - summary sums correctly")
    void analyzeFromText_multipleFoods() {
        FoodItem food1 = createFoodItem("rice", 300);
        FoodItem food2 = createFoodItem("chicken", 150);
        FoodItem food3 = createFoodItem("broccoli", 80);

        FoodIdentificationResult identified = new FoodIdentificationResult(
                List.of(food1, food2, food3), "lunch", 0.85);

        when(geminiService.identifyFoodFromText(anyString())).thenReturn(identified);
        when(nutritionLookupService.lookup(any()))
                .thenReturn(NutritionData.fromUSDA(390, 7.2, 0.6, 86, 0.6))
                .thenReturn(NutritionData.fromUSDA(247.5, 46.5, 5.4, 0, 0))
                .thenReturn(NutritionData.fromAIEstimate(28, 2.3, 0.3, 5.6, 2.1));
        doNothing().when(postProcessor).validate(any());

        FoodAnalysisResult result = foodAnalysisService.analyzeFromText("chicken rice broccoli");

        assertEquals(3, result.getFoods().size());
        assertEquals(3, result.getSummary().getFoodCount());

        // Summary should be sum of all foods
        double expectedCal = 390 + 247.5 + 28;
        assertEquals(expectedCal, result.getSummary().getTotalCalories(), 0.1);
    }

    @Test
    @DisplayName("Mixed data sources - each food tracks its own source")
    void analyzeFromText_mixedSources() {
        FoodItem food1 = createFoodItem("chicken", 100);
        FoodItem food2 = createFoodItem("exotic food", 100);

        FoodIdentificationResult identified = new FoodIdentificationResult(
                List.of(food1, food2), "mixed", 0.8);

        when(geminiService.identifyFoodFromText(anyString())).thenReturn(identified);
        when(nutritionLookupService.lookup(any()))
                .thenReturn(NutritionData.fromUSDA(165, 31, 3.6, 0, 0))
                .thenReturn(NutritionData.fromAIEstimate(200, 15, 10, 20, 3));
        doNothing().when(postProcessor).validate(any());

        FoodAnalysisResult result = foodAnalysisService.analyzeFromText("chicken and exotic food");

        assertEquals("USDA", result.getFoods().get(0).getDataSource());
        assertEquals("AI_ESTIMATE", result.getFoods().get(1).getDataSource());
    }

    @Test
    @DisplayName("Empty food list - returns empty result")
    void analyzeFromText_noFoods() {
        FoodIdentificationResult identified = new FoodIdentificationResult(
                List.of(), "", 0.1);

        when(geminiService.identifyFoodFromText(anyString())).thenReturn(identified);

        FoodAnalysisResult result = foodAnalysisService.analyzeFromText("not food");

        assertNotNull(result);
        assertTrue(result.getFoods().isEmpty());
        assertEquals(0, result.getSummary().getFoodCount());
    }

    @Test
    @DisplayName("Meal summary calculation - sums all nutrition values")
    void calculateMealSummary_correctSums() {
        FoodNutritionDetail d1 = new FoodNutritionDetail();
        d1.setCalories(200.0);
        d1.setProteinG(20.0);
        d1.setFatG(10.0);
        d1.setCarbsG(15.0);
        d1.setFiberG(3.0);

        FoodNutritionDetail d2 = new FoodNutritionDetail();
        d2.setCalories(300.0);
        d2.setProteinG(25.0);
        d2.setFatG(12.0);
        d2.setCarbsG(30.0);
        d2.setFiberG(5.0);

        MealSummary summary = FoodAnalysisService.calculateMealSummary(List.of(d1, d2));

        assertEquals(500.0, summary.getTotalCalories());
        assertEquals(45.0, summary.getTotalProteinG());
        assertEquals(22.0, summary.getTotalFatG());
        assertEquals(45.0, summary.getTotalCarbsG());
        assertEquals(8.0, summary.getTotalFiberG());
        assertEquals(2, summary.getFoodCount());
    }
}
