package com.foodprint.food.dto;

import java.time.LocalDate;

public class DailyNutrition {
    private LocalDate date;
    private double calories;
    private double protein;
    private double fat;
    private double carbs;

    public DailyNutrition() {
    }

    public DailyNutrition(LocalDate date, double calories, double protein, double fat, double carbs) {
        this.date = date;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }
}
