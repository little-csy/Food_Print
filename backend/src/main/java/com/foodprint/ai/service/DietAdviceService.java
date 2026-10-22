package com.foodprint.ai.service;

import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.UserNutritionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Generates personalized diet advice and meal plans using Gemini.
 * Requires UserNutritionContext from upstream modules.
 */
@Service
public class DietAdviceService {

    private static final Logger log = LoggerFactory.getLogger(DietAdviceService.class);

    private final GeminiService geminiService;

    public DietAdviceService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    /**
     * Generate diet advice. Returns PROFILE_NEEDED if user profile is incomplete.
     */
    public AIResponse generateAdvice(UserNutritionContext ctx) {
        if (ctx == null || !ctx.isProfileComplete()) {
            log.info("User profile incomplete, requesting profile completion");
            return AIResponse.needProfile(
                    "To give you personalized diet advice, we need your height, weight, and fitness goal. " +
                            "Please complete your profile first.");
        }

        String advice = geminiService.generateDietAdvice(ctx);
        return AIResponse.dietAdvice(advice);
    }

    /**
     * Generate a meal plan. Returns PROFILE_NEEDED if user profile is incomplete.
     */
    public AIResponse generatePlan(UserNutritionContext ctx) {
        if (ctx == null || !ctx.isProfileComplete()) {
            return AIResponse.needProfile(
                    "To create a meal plan, we need your height, weight, and fitness goal. " +
                            "Please complete your profile first.");
        }

        // For now, use the same advice generation with a plan-oriented prompt
        String plan = geminiService.generateDietAdvice(ctx);
        return AIResponse.dietPlan(plan);
    }

    /**
     * Generate a diet roast (funny critique). Returns PROFILE_NEEDED if incomplete.
     */
    public AIResponse generateRoast(UserNutritionContext ctx) {
        if (ctx == null || !ctx.isProfileComplete()) {
            return AIResponse.needProfile(
                    "To roast your diet, we need your height, weight, and fitness goal. " +
                            "Please complete your profile first so we can properly judge you.");
        }

        String roast = geminiService.generateDietRoast(ctx);
        return AIResponse.dietRoast(roast);
    }
}
