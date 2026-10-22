package com.foodprint.ai.dto;

/**
 * Nutrition data from database lookup or AI estimate.
 */
public class NutritionData {
    private double calories;
    private double proteinG;
    private double fatG;
    private double carbsG;
    private double fiberG;
    private String source; // "USDA" or "AI_ESTIMATE"

    public NutritionData() {
    }

    public NutritionData(double calories, double proteinG, double fatG, double carbsG, double fiberG, String source) {
        this.calories = calories;
        this.proteinG = proteinG;
        this.fatG = fatG;
        this.carbsG = carbsG;
        this.fiberG = fiberG;
        this.source = source;
    }

    public static NutritionData fromAIEstimate(double calories, double proteinG, double fatG, double carbsG,
            double fiberG) {
        return new NutritionData(calories, proteinG, fatG, carbsG, fiberG, "AI_ESTIMATE");
    }

    public static NutritionData fromUSDA(double calories, double proteinG, double fatG, double carbsG, double fiberG) {
        return new NutritionData(calories, proteinG, fatG, carbsG, fiberG, "USDA");
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProteinG() {
        return proteinG;
    }

    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    public double getFatG() {
        return fatG;
    }

    public void setFatG(double fatG) {
        this.fatG = fatG;
    }

    public double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(double carbsG) {
        this.carbsG = carbsG;
    }

    public double getFiberG() {
        return fiberG;
    }

    public void setFiberG(double fiberG) {
        this.fiberG = fiberG;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
