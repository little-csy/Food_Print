package com.foodprint.food.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Enhanced DTO for the "Today's Meals" list on the home screen.
 * Includes a list of food item names for a richer summary.
 */
public class TodayMealResponse {
    private UUID id;
    private String title;
    private List<String> foodItems;
    private Double totalCalories;
    private LocalDateTime mealTime;

    public TodayMealResponse() {}

    public TodayMealResponse(UUID id, String title, List<String> foodItems, Double totalCalories, LocalDateTime mealTime) {
        this.id = id;
        this.title = title;
        this.foodItems = foodItems;
        this.totalCalories = totalCalories;
        this.mealTime = mealTime;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getFoodItems() { return foodItems; }
    public void setFoodItems(List<String> foodItems) { this.foodItems = foodItems; }

    public Double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(Double totalCalories) { this.totalCalories = totalCalories; }

    public LocalDateTime getMealTime() { return mealTime; }
    public void setMealTime(LocalDateTime mealTime) { this.mealTime = mealTime; }
}
