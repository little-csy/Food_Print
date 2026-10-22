package com.foodprint.ai.service;

import com.foodprint.ai.dto.*;
import com.foodprint.ai.dto.FoodAnalysisResult.FoodNutritionDetail;
import com.foodprint.ai.dto.FoodAnalysisResult.MealSummary;
import com.foodprint.ai.dto.FoodIdentificationResult.FoodItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full food analysis pipeline:
 * Gemini identification → USDA lookup → post-processing → summary.
 */
@Service
public class FoodAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(FoodAnalysisService.class);

    private final GeminiService geminiService;
    private final NutritionLookupService nutritionLookupService;
    private final NutritionPostProcessor postProcessor;

    public FoodAnalysisService(GeminiService geminiService,
            NutritionLookupService nutritionLookupService,
            NutritionPostProcessor postProcessor) {
        this.geminiService = geminiService;
        this.nutritionLookupService = nutritionLookupService;
        this.postProcessor = postProcessor;
    }

    /**
     * Analyze food from an image.
     */
    public FoodAnalysisResult analyzeFromImage(byte[] imageData, String mimeType) {
        log.info("Analyzing food from image (size={} bytes, type={})", imageData.length, mimeType);
        FoodIdentificationResult identified = geminiService.identifyFoodFromImage(imageData, mimeType);
        return buildAnalysisResult(identified);
    }

    /**
     * Analyze food from a text description.
     */
    public FoodAnalysisResult analyzeFromText(String description) {
        log.info("Analyzing food from text: '{}'",
                description.length() > 50 ? description.substring(0, 50) + "..." : description);
        FoodIdentificationResult identified = geminiService.identifyFoodFromText(description);
        return buildAnalysisResult(identified);
    }

    private FoodAnalysisResult buildAnalysisResult(FoodIdentificationResult identified) {
        if (identified.getFoods() == null || identified.getFoods().isEmpty()) {
            log.warn("No foods identified, returning empty result");
            MealSummary emptySummary = new MealSummary();
            emptySummary.setFoodCount(0);
            return new FoodAnalysisResult(List.of(), emptySummary, identified.getConfidence());
        }

        List<FoodNutritionDetail> details = new ArrayList<>();

        for (FoodItem food : identified.getFoods()) {
            // Look up nutrition data (USDA → AI fallback)
            NutritionData nutrition = nutritionLookupService.lookup(food);

            // Merge AI identification with nutrition data
            FoodNutritionDetail detail = merge(food, nutrition);

            // Post-process: validate and flag anomalies
            postProcessor.validate(detail);

            details.add(detail);
        }

        MealSummary summary = calculateMealSummary(details);
        return new FoodAnalysisResult(details, summary, identified.getConfidence());
    }

    private FoodNutritionDetail merge(FoodItem food, NutritionData nutrition) {
        FoodNutritionDetail detail = new FoodNutritionDetail();
        detail.setNameEn(food.getNameEn());
        detail.setNameZh(food.getNameZh());
        detail.setPortionAmount(food.getPortionAmount());
        detail.setPortionUnit(food.getPortionUnit());
        detail.setCalories(nutrition.getCalories());
        detail.setProteinG(nutrition.getProteinG());
        detail.setFatG(nutrition.getFatG());
        detail.setCarbsG(nutrition.getCarbsG());
        detail.setFiberG(nutrition.getFiberG());
        detail.setDataSource(nutrition.getSource());
        return detail;
    }

    static MealSummary calculateMealSummary(List<FoodNutritionDetail> details) {
        MealSummary summary = new MealSummary();
        summary.setFoodCount(details.size());

        double totalCal = 0, totalProtein = 0, totalFat = 0, totalCarbs = 0, totalFiber = 0;
        for (FoodNutritionDetail d : details) {
            totalCal += d.getCalories();
            totalProtein += d.getProteinG();
            totalFat += d.getFatG();
            totalCarbs += d.getCarbsG();
            totalFiber += d.getFiberG();
        }

        summary.setTotalCalories(Math.round(totalCal * 10.0) / 10.0);
        summary.setTotalProteinG(Math.round(totalProtein * 10.0) / 10.0);
        summary.setTotalFatG(Math.round(totalFat * 10.0) / 10.0);
        summary.setTotalCarbsG(Math.round(totalCarbs * 10.0) / 10.0);
        summary.setTotalFiberG(Math.round(totalFiber * 10.0) / 10.0);

        return summary;
    }
}
