package com.foodprint.ai.dto;

/**
 * User context passed from upstream modules for personalized diet advice.
 * AI module does NOT query the database — this is injected by the controller.
 */
public class UserNutritionContext {
    // User profile (from User module)
    private Double heightCm;
    private Double weightKg;
    private Integer age;
    private String gender;
    private String goal; // LOSE_WEIGHT / MAINTAIN / GAIN_MUSCLE
    private String dietaryPreference; // e.g. Vegetarian, Keto, Halal
    private Integer dailyCalorieTarget;
    private String userId; // UUID string for Chat History tracking

    // Today's intake (from Food Record module)
    private Double todayCalories;
    private Double todayProtein;
    private Double todayFat;
    private Double todayCarbs;

    public UserNutritionContext() {
    }

    /**
     * Check if we have enough profile data to give meaningful advice.
     */
    public boolean isProfileComplete() {
        return heightCm != null && weightKg != null && goal != null;
    }

    // Getters and Setters
    public Double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Double heightCm) {
        this.heightCm = heightCm;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Double weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getDietaryPreference() {
        return dietaryPreference;
    }

    public void setDietaryPreference(String dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }

    public Integer getDailyCalorieTarget() {
        return dailyCalorieTarget;
    }

    public void setDailyCalorieTarget(Integer dailyCalorieTarget) {
        this.dailyCalorieTarget = dailyCalorieTarget;
    }

    public Double getTodayCalories() {
        return todayCalories;
    }

    public void setTodayCalories(Double todayCalories) {
        this.todayCalories = todayCalories;
    }

    public Double getTodayProtein() {
        return todayProtein;
    }

    public void setTodayProtein(Double todayProtein) {
        this.todayProtein = todayProtein;
    }

    public Double getTodayFat() {
        return todayFat;
    }

    public void setTodayFat(Double todayFat) {
        this.todayFat = todayFat;
    }

    public Double getTodayCarbs() {
        return todayCarbs;
    }

    public void setTodayCarbs(Double todayCarbs) {
        this.todayCarbs = todayCarbs;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
