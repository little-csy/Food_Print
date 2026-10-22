package com.foodprint.ai.dto;

/**
 * Unified AI response that wraps all possible response types.
 */
public class AIResponse {
    private String type; // FOOD_ANALYSIS / DIET_ADVICE / DIET_PLAN / CHAT
    private FoodAnalysisResult foodAnalysis;
    private String adviceText;
    private boolean profileIncomplete;
    private String profilePrompt;

    private AIResponse() {
    }

    public static AIResponse foodAnalysis(FoodAnalysisResult result) {
        AIResponse resp = new AIResponse();
        resp.type = "FOOD_ANALYSIS";
        resp.foodAnalysis = result;
        return resp;
    }

    public static AIResponse dietAdvice(String advice) {
        AIResponse resp = new AIResponse();
        resp.type = "DIET_ADVICE";
        resp.adviceText = advice;
        return resp;
    }

    public static AIResponse dietPlan(String plan) {
        AIResponse resp = new AIResponse();
        resp.type = "DIET_PLAN";
        resp.adviceText = plan;
        return resp;
    }

    public static AIResponse dietRoast(String roast) {
        AIResponse resp = new AIResponse();
        resp.type = "DIET_ROAST";
        resp.adviceText = roast;
        return resp;
    }

    public static AIResponse chat(String message) {
        AIResponse resp = new AIResponse();
        resp.type = "CHAT";
        resp.adviceText = message;
        return resp;
    }

    public static AIResponse needProfile(String prompt) {
        AIResponse resp = new AIResponse();
        resp.type = "PROFILE_NEEDED";
        resp.profileIncomplete = true;
        resp.profilePrompt = prompt;
        return resp;
    }

    // Getters
    public String getType() {
        return type;
    }

    public FoodAnalysisResult getFoodAnalysis() {
        return foodAnalysis;
    }

    public String getAdviceText() {
        return adviceText;
    }

    public boolean isProfileIncomplete() {
        return profileIncomplete;
    }

    public String getProfilePrompt() {
        return profilePrompt;
    }
}
