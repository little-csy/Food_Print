package com.foodprint.ai.dto;

import java.time.LocalDateTime;

/**
 * Request to save an AI analysis result as a persistable food log.
 */
public class SaveAIAnalysisLogRequest {
    private String title;
    private String imageUrl;
    private LocalDateTime mealTime;
    private FoodAnalysisResult analysisResult;

    public SaveAIAnalysisLogRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getMealTime() { return mealTime; }
    public void setMealTime(LocalDateTime mealTime) { this.mealTime = mealTime; }

    public FoodAnalysisResult getAnalysisResult() { return analysisResult; }
    public void setAnalysisResult(FoodAnalysisResult analysisResult) { this.analysisResult = analysisResult; }
}
