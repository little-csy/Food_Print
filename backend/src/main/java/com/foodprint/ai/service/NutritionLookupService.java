package com.foodprint.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.dto.FoodIdentificationResult.FoodItem;
import com.foodprint.ai.dto.NutritionData;
import com.foodprint.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Looks up nutrition data from USDA FoodData Central.
 * Falls back to AI estimates if USDA has no results or is unavailable.
 */
@Service
public class NutritionLookupService {

    private static final Logger log = LoggerFactory.getLogger(NutritionLookupService.class);

    // USDA nutrient IDs
    private static final int NUTRIENT_ENERGY = 1008; // Energy (kcal)
    private static final int NUTRIENT_PROTEIN = 1003; // Protein (g)
    private static final int NUTRIENT_FAT = 1004; // Total fat (g)
    private static final int NUTRIENT_CARBS = 1005; // Carbohydrate (g)
    private static final int NUTRIENT_FIBER = 1079; // Fiber (g)

    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;

    public NutritionLookupService(@Qualifier("usdaWebClient") WebClient webClient,
            AIConfig aiConfig,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.aiConfig = aiConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Look up nutrition data for a food item.
     * Priority: USDA → AI estimate fallback.
     *
     * @param foodItem the food item identified by AI (contains name + estimates)
     * @return nutrition data with source tracking
     */
    public NutritionData lookup(FoodItem foodItem) {
        try {
            NutritionData usdaResult = searchUSDA(foodItem.getNameEn(), foodItem.getPortionAmount(),
                    foodItem.getPortionUnit());
            if (usdaResult != null) {
                return usdaResult;
            }
        } catch (Exception e) {
            log.warn("USDA lookup failed for '{}', falling back to AI estimate: {}", foodItem.getNameEn(),
                    e.getMessage());
        }

        // Fallback: use AI estimates
        log.info("Using AI estimate for '{}'", foodItem.getNameEn());
        return NutritionData.fromAIEstimate(
                foodItem.getEstimatedCalories(),
                foodItem.getEstimatedProteinG(),
                foodItem.getEstimatedFatG(),
                foodItem.getEstimatedCarbsG(),
                foodItem.getEstimatedFiberG());
    }

    /**
     * Search USDA FoodData Central for nutrition data.
     * Returns null if no results found.
     */
    NutritionData searchUSDA(String foodName, double portionAmount, String portionUnit) {
        String apiKey = aiConfig.getUsda().getApiKey();

        Map<String, Object> searchBody = Map.of(
                "query", foodName,
                "pageSize", 3,
                "dataType", java.util.List.of("Survey (FNDDS)", "SR Legacy"));

        try {
            String response = webClient.post()
                    .uri("/foods/search?api_key={key}", apiKey)
                    .bodyValue(searchBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseUSDAResponse(response, portionAmount, portionUnit);
        } catch (WebClientResponseException e) {
            log.error("USDA API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("USDA API call failed", "USDA_API_ERROR", e);
        }
    }

    NutritionData parseUSDAResponse(String responseJson, double portionAmount, String portionUnit) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode foods = root.path("foods");

            if (!foods.isArray() || foods.isEmpty()) {
                return null;
            }

            // Take the first (most relevant) result
            JsonNode firstFood = foods.get(0);
            JsonNode nutrients = firstFood.path("foodNutrients");

            double caloriesPer100g = 0, proteinPer100g = 0, fatPer100g = 0;
            double carbsPer100g = 0, fiberPer100g = 0;

            for (JsonNode nutrient : nutrients) {
                int id = nutrient.path("nutrientId").asInt();
                double value = nutrient.path("value").asDouble(0);

                switch (id) {
                    case NUTRIENT_ENERGY -> caloriesPer100g = value;
                    case NUTRIENT_PROTEIN -> proteinPer100g = value;
                    case NUTRIENT_FAT -> fatPer100g = value;
                    case NUTRIENT_CARBS -> carbsPer100g = value;
                    case NUTRIENT_FIBER -> fiberPer100g = value;
                }
            }

            // Convert to actual portion
            double portionInGrams = convertToGrams(portionAmount, portionUnit);
            double factor = portionInGrams / 100.0;

            return NutritionData.fromUSDA(
                    Math.round(caloriesPer100g * factor * 10.0) / 10.0,
                    Math.round(proteinPer100g * factor * 10.0) / 10.0,
                    Math.round(fatPer100g * factor * 10.0) / 10.0,
                    Math.round(carbsPer100g * factor * 10.0) / 10.0,
                    Math.round(fiberPer100g * factor * 10.0) / 10.0);
        } catch (Exception e) {
            log.error("Failed to parse USDA response", e);
            return null;
        }
    }

    /**
     * Convert common portion units to grams.
     */
    static double convertToGrams(double amount, String unit) {
        if (unit == null)
            return amount;
        return switch (unit.toLowerCase().trim()) {
            case "g", "gram", "grams" -> amount;
            case "kg", "kilogram" -> amount * 1000;
            case "oz", "ounce", "ounces" -> amount * 28.35;
            case "lb", "pound", "pounds" -> amount * 453.6;
            case "cup", "cups" -> amount * 240;
            case "tbsp", "tablespoon", "tablespoons" -> amount * 15;
            case "tsp", "teaspoon", "teaspoons" -> amount * 5;
            case "ml", "milliliter", "milliliters" -> amount; // approximate 1ml ≈ 1g
            case "l", "liter", "liters" -> amount * 1000;
            case "slice", "slices" -> amount * 30; // approximate
            case "piece", "pieces" -> amount * 100; // approximate
            case "bowl" -> amount * 300; // approximate
            default -> amount; // assume grams
        };
    }
}
