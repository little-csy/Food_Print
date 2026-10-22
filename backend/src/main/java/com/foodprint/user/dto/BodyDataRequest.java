package com.foodprint.user.dto;

import lombok.Data;

@Data
public class BodyDataRequest {
    private Double heightCm;
    private Double weightKg;
    private Integer age;
    private String gender;
    private String goal;
    private Double dailyCalorieTarget;
    private String dietaryPreference;
}
