package com.foodprint.food.dto;

/**
 * Request body for creating a food log.
 * The client can provide either text description or an image URL.
 */
public class CreateFoodLogRequest {

    // User text input describing the meal
    private String text;

    // Image URL uploaded to Supabase Storage
    private String imageUrl;

    // Optional date of the meal (defaults to today if null)
    private String date;

    public CreateFoodLogRequest() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
