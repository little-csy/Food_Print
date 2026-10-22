package com.foodprint.ai.service;

import com.foodprint.ai.dto.FoodAnalysisResult.FoodNutritionDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Post-processes nutrition data for validation and anomaly detection.
 */
@Service
public class NutritionPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(NutritionPostProcessor.class);

    private static final double MAX_SINGLE_FOOD_CALORIES = 2000;
    private static final double MACRO_CONSISTENCY_THRESHOLD = 0.25; // 25% tolerance

    /**
     * Validate and flag anomalies in nutrition data.
     * Modifies the detail in-place by setting flagged/flagReason.
     */
    public void validate(FoodNutritionDetail detail) {
        // Check 1: Calorie range
        if (detail.getCalories() > MAX_SINGLE_FOOD_CALORIES) {
            flag(detail, "Unusually high calories (" + detail.getCalories() + " kcal) for a single food item");
            return;
        }

        if (detail.getCalories() < 0) {
            flag(detail, "Negative calorie value");
            return;
        }

        // Check 2: Macro consistency (calories ≈ protein*4 + fat*9 + carbs*4)
        double expectedCalories = detail.getProteinG() * 4 + detail.getFatG() * 9 + detail.getCarbsG() * 4;
        if (expectedCalories > 0 && detail.getCalories() > 0) {
            double ratio = Math.abs(detail.getCalories() - expectedCalories) / expectedCalories;
            if (ratio > MACRO_CONSISTENCY_THRESHOLD) {
                flag(detail, String.format(
                        "Calorie/macro mismatch: reported %.0f kcal, expected ~%.0f kcal from macros (%.0f%% off)",
                        detail.getCalories(), expectedCalories, ratio * 100));
                return;
            }
        }

        // Check 3: Negative macros
        if (detail.getProteinG() < 0 || detail.getFatG() < 0 || detail.getCarbsG() < 0) {
            flag(detail, "Negative macro nutrient value");
            return;
        }

        // Passed all checks
        detail.setFlagged(false);
        detail.setFlagReason(null);
    }

    private void flag(FoodNutritionDetail detail, String reason) {
        log.warn("Nutrition anomaly for '{}': {}", detail.getNameEn(), reason);
        detail.setFlagged(true);
        detail.setFlagReason(reason);
    }
}
