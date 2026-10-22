package com.foodprint.challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A snapshot of the user's full daily nutrition intake and activity.
 * Included in CheckinResponse and TodayStatusResponse so the frontend
 * can display whichever metrics are relevant to the current challenge.
 */
public class NutritionSnapshot {

    /** Total calories consumed from food logs today */
    private double calories;

    /** Total protein consumed today (grams) */
    @JsonProperty("protein_g")
    private double proteinG;

    /** Total fat consumed today (grams) */
    @JsonProperty("fat_g")
    private double fatG;

    /** Total carbohydrates consumed today (grams) */
    @JsonProperty("carbs_g")
    private double carbsG;

    /** Total dietary fiber consumed today (grams) */
    @JsonProperty("fiber_g")
    private double fiberG;

    /** Total water logged today (ml) */
    @JsonProperty("water_ml")
    private double waterMl;

    /** Total calories burned via exercise today */
    @JsonProperty("calories_burned")
    private double caloriesBurned;

    /** Number of food log entries recorded today */
    @JsonProperty("food_log_count")
    private int foodLogCount;

    public NutritionSnapshot() {}

    public NutritionSnapshot(double calories, double proteinG, double fatG, double carbsG,
                              double fiberG, double waterMl, double caloriesBurned,
                              int foodLogCount) {
        this.calories = calories;
        this.proteinG = proteinG;
        this.fatG = fatG;
        this.carbsG = carbsG;
        this.fiberG = fiberG;
        this.waterMl = waterMl;
        this.caloriesBurned = caloriesBurned;
        this.foodLogCount = foodLogCount;
    }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProteinG() { return proteinG; }
    public void setProteinG(double proteinG) { this.proteinG = proteinG; }

    public double getFatG() { return fatG; }
    public void setFatG(double fatG) { this.fatG = fatG; }

    public double getCarbsG() { return carbsG; }
    public void setCarbsG(double carbsG) { this.carbsG = carbsG; }

    public double getFiberG() { return fiberG; }
    public void setFiberG(double fiberG) { this.fiberG = fiberG; }

    public double getWaterMl() { return waterMl; }
    public void setWaterMl(double waterMl) { this.waterMl = waterMl; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public int getFoodLogCount() { return foodLogCount; }
    public void setFoodLogCount(int foodLogCount) { this.foodLogCount = foodLogCount; }
}
