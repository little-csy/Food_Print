package com.foodprint.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.dto.FoodIdentificationResult;
import com.foodprint.ai.dto.FoodIdentificationResult.FoodItem;
import com.foodprint.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

/**
 * Service for calling Google Gemini API.
 * Handles both image and text inputs for food identification,
 * as well as diet advice generation and general chat.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;

    // Prompt templates
    static final String FOOD_IMAGE_PROMPT = """
            You are a professional nutritionist. Analyze this food image.
            Return ONLY a valid JSON object in this exact format, no other text, no markdown:
            {
              "foods": [
                {
                  "name_en": "food name in English",
                  "name_zh": "food name in Chinese",
                  "portion_amount": 150,
                  "portion_unit": "g",
                  "category": "protein",
                  "estimated_calories": 248,
                  "estimated_protein_g": 46.5,
                  "estimated_fat_g": 5.4,
                  "estimated_carbs_g": 0,
                  "estimated_fiber_g": 0
                }
              ],
              "meal_description": "brief description of the meal",
              "confidence": 0.85
            }
            Rules:
            - List EVERY distinct food item visible in the image
            - Use standard portion sizes if unclear from the image
            - estimated_ fields are your best nutritional estimates
            - confidence: 0.0-1.0, how certain you are about the identification
            - portion_unit should be "g" or "ml"
            """;

    static final String FOOD_TEXT_PROMPT = """
            You are a professional nutritionist. The user describes what they ate:
            "%s"
            Parse this into structured food items. Return ONLY a valid JSON object in this exact format, no other text, no markdown:
            {
              "foods": [
                {
                  "name_en": "food name in English",
                  "name_zh": "food name in Chinese",
                  "portion_amount": 150,
                  "portion_unit": "g",
                  "category": "protein",
                  "estimated_calories": 248,
                  "estimated_protein_g": 46.5,
                  "estimated_fat_g": 5.4,
                  "estimated_carbs_g": 0,
                  "estimated_fiber_g": 0
                }
              ],
              "meal_description": "brief description",
              "confidence": 0.85
            }
            Rules:
            - Parse every food item the user mentioned
            - Estimate portion sizes from context clues (e.g., "a bowl of" ≈ 300g, "a slice" ≈ 30g)
            - estimated_ fields are your best nutritional estimates
            - confidence: 0.0-1.0
            - portion_unit should be "g" or "ml"
            """;

    static final String INTENT_PROMPT = """
            Classify the user's intent into exactly one category.
            User message: "%s"

            Categories:
            - FOOD_LOG: User is describing food they ate, are eating, or plan to eat for logging purposes
            - DIET_ADVICE: User is asking for diet, nutrition, or health advice
            - DIET_PLAN: User wants a meal plan, diet schedule, or weekly menu
            - DIET_ROAST: User wants a harsh, funny, or tough-love critique/roast of their diet
            - GENERAL_CHAT: General conversation about food, health, or anything else

            Return ONLY the category name, nothing else.
            """;

    static final String DIET_ADVICE_PROMPT = """
            You are a professional nutritionist and health advisor.

            User Profile:
            - Height: %s cm, Weight: %s kg, Age: %s, Gender: %s
            - Dietary Preference: %s
            - Goal: %s
            - Daily Calorie Target: %s kcal

            Today's Intake So Far:
            - Calories: %s kcal
            - Protein: %s g
            - Fat: %s g
            - Carbs: %s g

            Based on the user's profile and today's intake, provide personalized diet advice.
            Be specific, actionable, and encouraging. Include:
            1. Assessment of today's intake vs their goal
            2. What they should eat for the rest of the day
            3. General tips aligned with their goal

            Respond in a friendly, supportive tone.
            """;

    static final String DIET_ROAST_PROMPT = """
            You are a brutal, sarcastic, but ultimately caring fitness coach/comedian.

            User Profile:
            - Height: %s cm, Weight: %s kg, Age: %s, Gender: %s
            - Dietary Preference: %s
            - Goal: %s
            - Daily Calorie Target: %s kcal

            Today's Intake So Far:
            - Calories: %s kcal
            - Protein: %s g
            - Fat: %s g
            - Carbs: %s g

            ROAST THIS USER's diet today based on their goals and what they've eaten.
            Be funny, sassy, and brutally honest, but don't be genuinely offensive.
            Use Gen Z slang if appropriate. Tell them what they are doing wrong and
            give them tough love on what they SHOULD eat for the rest of the day to fix this mess.
            """;

    public GeminiService(@Qualifier("geminiWebClient") WebClient webClient,
            AIConfig aiConfig,
            ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.aiConfig = aiConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Identify foods from an image using Gemini Vision.
     */
    public FoodIdentificationResult identifyFoodFromImage(byte[] imageData, String mimeType) {
        String base64Image = Base64.getEncoder().encodeToString(imageData);

        Map<String, Object> requestBody = buildImageRequest(base64Image, mimeType, FOOD_IMAGE_PROMPT);
        String responseText = callGemini(requestBody);
        return parseFoodIdentificationResult(responseText);
    }

    /**
     * Identify foods from a text description using Gemini.
     */
    public FoodIdentificationResult identifyFoodFromText(String description) {
        String prompt = String.format(FOOD_TEXT_PROMPT, description);
        Map<String, Object> requestBody = buildTextRequest(prompt);
        String responseText = callGemini(requestBody);
        return parseFoodIdentificationResult(responseText);
    }

    /**
     * Classify user intent from text message.
     */
    public UserIntent classifyIntent(String message) {
        String prompt = String.format(INTENT_PROMPT, message);
        Map<String, Object> requestBody = buildTextRequest(prompt);

        try {
            String responseText = callGemini(requestBody).trim().toUpperCase();
            return UserIntent.valueOf(responseText);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse intent '{}', defaulting to GENERAL_CHAT", e.getMessage());
            return UserIntent.GENERAL_CHAT;
        }
    }

    /**
     * Generate diet advice based on user context.
     */
    public String generateDietAdvice(com.foodprint.ai.dto.UserNutritionContext ctx) {
        String prompt = String.format(DIET_ADVICE_PROMPT,
                ctx.getHeightCm(), ctx.getWeightKg(), ctx.getAge(), ctx.getGender(),
                ctx.getDietaryPreference() != null ? ctx.getDietaryPreference() : "None",
                ctx.getGoal(), ctx.getDailyCalorieTarget(),
                ctx.getTodayCalories(), ctx.getTodayProtein(),
                ctx.getTodayFat(), ctx.getTodayCarbs());

        Map<String, Object> requestBody = buildTextRequest(prompt);
        return callGemini(requestBody);
    }

    /**
     * Generate a harsh, funny diet roast based on user context.
     */
    public String generateDietRoast(com.foodprint.ai.dto.UserNutritionContext ctx) {
        String prompt = String.format(DIET_ROAST_PROMPT,
                ctx.getHeightCm(), ctx.getWeightKg(), ctx.getAge(), ctx.getGender(),
                ctx.getDietaryPreference() != null ? ctx.getDietaryPreference() : "None",
                ctx.getGoal(), ctx.getDailyCalorieTarget(),
                ctx.getTodayCalories(), ctx.getTodayProtein(),
                ctx.getTodayFat(), ctx.getTodayCarbs());

        Map<String, Object> requestBody = buildTextRequest(prompt);
        return callGemini(requestBody);
    }

    /**
     * General chat response.
     */
    public String chat(String message) {
        String prompt = "You are a friendly health and nutrition assistant for the FoodPrint app. " +
                "Keep responses helpful, concise, and related to food and health. " +
                "User says: \"" + message + "\"";

        Map<String, Object> requestBody = buildTextRequest(prompt);
        return callGemini(requestBody);
    }

    // ---- Internal helpers ----

    private Map<String, Object> buildTextRequest(String text) {
        Map<String, Object> textPart = Map.of("text", text);
        Map<String, Object> content = Map.of("parts", List.of(textPart));
        return Map.of("contents", List.of(content));
    }

    private Map<String, Object> buildImageRequest(String base64Image, String mimeType, String prompt) {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> imagePart = Map.of(
                "inline_data", Map.of(
                        "mime_type", mimeType,
                        "data", base64Image));
        Map<String, Object> content = Map.of("parts", List.of(textPart, imagePart));
        return Map.of("contents", List.of(content));
    }

    String callGemini(Map<String, Object> requestBody) {
        String model = aiConfig.getGemini().getModel();
        String apiKey = aiConfig.getGemini().getApiKey();

        try {
            String response = webClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (WebClientResponseException e) {
            log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("Gemini API call failed: " + e.getMessage(), "GEMINI_API_ERROR", e);
        } catch (Exception e) {
            log.error("Gemini call failed", e);
            throw new AIServiceException("Gemini call failed: " + e.getMessage(), "GEMINI_ERROR", e);
        }
    }

    String extractTextFromResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new AIServiceException("Gemini returned empty candidates", "GEMINI_EMPTY_RESPONSE");
            }
            return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (JsonProcessingException e) {
            throw new AIServiceException("Failed to parse Gemini response", "GEMINI_PARSE_ERROR", e);
        }
    }

    FoodIdentificationResult parseFoodIdentificationResult(String text) {
        try {
            // Strip markdown code fences if present
            String json = text.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("\\n?```$", "").trim();
            }

            JsonNode root = objectMapper.readTree(json);

            List<FoodItem> foods = new ArrayList<>();
            JsonNode foodsNode = root.path("foods");
            if (foodsNode.isArray()) {
                for (JsonNode foodNode : foodsNode) {
                    FoodItem item = new FoodItem();
                    item.setNameEn(foodNode.path("name_en").asText("unknown"));
                    item.setNameZh(foodNode.path("name_zh").asText(""));
                    item.setPortionAmount(foodNode.path("portion_amount").asDouble(100));
                    item.setPortionUnit(foodNode.path("portion_unit").asText("g"));
                    item.setCategory(foodNode.path("category").asText("other"));
                    item.setEstimatedCalories(foodNode.path("estimated_calories").asDouble(0));
                    item.setEstimatedProteinG(foodNode.path("estimated_protein_g").asDouble(0));
                    item.setEstimatedFatG(foodNode.path("estimated_fat_g").asDouble(0));
                    item.setEstimatedCarbsG(foodNode.path("estimated_carbs_g").asDouble(0));
                    item.setEstimatedFiberG(foodNode.path("estimated_fiber_g").asDouble(0));
                    foods.add(item);
                }
            }

            String mealDesc = root.path("meal_description").asText("");
            double confidence = root.path("confidence").asDouble(0.5);

            return new FoodIdentificationResult(foods, mealDesc, confidence);
        } catch (JsonProcessingException e) {
            throw new AIServiceException("Failed to parse food identification JSON: " + text, "PARSE_ERROR", e);
        }
    }
}
