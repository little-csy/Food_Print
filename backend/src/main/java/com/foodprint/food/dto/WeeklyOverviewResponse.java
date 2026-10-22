package com.foodprint.food.dto;

import java.util.List;

public class WeeklyOverviewResponse {
    private List<DailyNutrition> days;
    private double averageCalPerDay;

    public WeeklyOverviewResponse() {
    }

    public WeeklyOverviewResponse(List<DailyNutrition> days, double averageCalPerDay) {
        this.days = days;
        this.averageCalPerDay = averageCalPerDay;
    }

    public List<DailyNutrition> getDays() {
        return days;
    }

    public void setDays(List<DailyNutrition> days) {
        this.days = days;
    }

    public double getAverageCalPerDay() {
        return averageCalPerDay;
    }

    public void setAverageCalPerDay(double averageCalPerDay) {
        this.averageCalPerDay = averageCalPerDay;
    }
}
