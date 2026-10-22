package com.foodprint.ai.service;

import com.foodprint.ai.dto.FoodAnalysisResult.FoodNutritionDetail;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure logic tests for NutritionPostProcessor.
 * No mocks needed — input/output only.
 */
class NutritionPostProcessorTest {

    private NutritionPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new NutritionPostProcessor();
    }

    private FoodNutritionDetail createDetail(String name, double cal, double protein, double fat, double carbs) {
        FoodNutritionDetail d = new FoodNutritionDetail();
        d.setNameEn(name);
        d.setCalories(cal);
        d.setProteinG(protein);
        d.setFatG(fat);
        d.setCarbsG(carbs);
        d.setFiberG(0.0);
        return d;
    }

    @Test
    @DisplayName("Normal values - passes validation")
    void validate_normalValues() {
        // 250 cal ≈ 30*4 + 5*9 + 20*4 = 120+45+80 = 245 → within 25%
        FoodNutritionDetail detail = createDetail("chicken", 250, 30, 5, 20);
        processor.validate(detail);
        assertFalse(detail.isFlagged());
    }

    @Test
    @DisplayName("Extremely high calories - flagged")
    void validate_highCalories() {
        FoodNutritionDetail detail = createDetail("egg", 5000, 6, 5, 1);
        processor.validate(detail);
        assertTrue(detail.isFlagged());
        assertTrue(detail.getFlagReason().contains("high calories"));
    }

    @Test
    @DisplayName("Negative calories - flagged")
    void validate_negativeCalories() {
        FoodNutritionDetail detail = createDetail("food", -50, 10, 5, 10);
        processor.validate(detail);
        assertTrue(detail.isFlagged());
    }

    @Test
    @DisplayName("Macro/calorie mismatch - flagged")
    void validate_macroMismatch() {
        // Reported 500 cal, but macros suggest: 10*4 + 5*9 + 10*4 = 40+45+40 = 125
        // Ratio: |500-125|/125 = 3.0 = 300% → flagged
        FoodNutritionDetail detail = createDetail("food", 500, 10, 5, 10);
        processor.validate(detail);
        assertTrue(detail.isFlagged());
        assertTrue(detail.getFlagReason().contains("mismatch"));
    }

    @Test
    @DisplayName("Negative macros - flagged")
    void validate_negativeMacros() {
        FoodNutritionDetail detail = createDetail("food", 100, -5, 10, 10);
        processor.validate(detail);
        assertTrue(detail.isFlagged());
    }

    @Test
    @DisplayName("Macro consistency within 25% - passes")
    void validate_macroConsistency_withinThreshold() {
        // Macros suggest: 30*4 + 10*9 + 40*4 = 120+90+160 = 370
        // Reported: 400 cal → ratio = |400-370|/370 ≈ 8% → passes
        FoodNutritionDetail detail = createDetail("meal", 400, 30, 10, 40);
        processor.validate(detail);
        assertFalse(detail.isFlagged());
    }
}
