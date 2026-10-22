package com.foodprint.food.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ManualMealLogRequest {
    private String title;
    private String imageUrl;
    private LocalDateTime mealTime;
    private List<MealItemDTO> foods;

    public ManualMealLogRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public LocalDateTime getMealTime() { return mealTime; }
    public void setMealTime(LocalDateTime mealTime) { this.mealTime = mealTime; }

    public List<MealItemDTO> getFoods() { return foods; }
    public void setFoods(List<MealItemDTO> foods) { this.foods = foods; }
}
