package com.foodprint.food.entity;

// TODO [Engineer B]: Implement NutritionData entity
// This stores per-food-item nutrition data from AI analysis results.

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "food_nutrition_details")
public class NutritionData {

    @Id
    private UUID id = UUID.randomUUID();

    // Foreign key referencing FoodLog
    @Column(name = "food_log_id", nullable = false)
    private UUID foodLogId;

    // English name of the food item
    @Column(name = "name_en")
    private String nameEn;

    // Chinese name of the food item
    @Column(name = "name_zh")
    private String nameZh;

    // Nutrition values
    @Column(name = "calories")
    private Double calories;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "carbs_g")
    private Double carbsG;

    @Column(name = "fiber_g")
    private Double fiberG;

    // Portion info
    @Column(name = "portion_amount")
    private Double portionAmount;

    @Column(name = "portion_unit")
    private String portionUnit;

    // Source of the nutrition data: "USDA" or "AI_ESTIMATE"
    @Column(name = "data_source")
    private String dataSource;

    // ===== Getters and Setters =====

    public UUID getId() {
        return id;
    }

    public UUID getFoodLogId() {
        return foodLogId;
    }

    public void setFoodLogId(UUID foodLogId) {
        this.foodLogId = foodLogId;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameZh() {
        return nameZh;
    }

    public void setNameZh(String nameZh) {
        this.nameZh = nameZh;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getProteinG() {
        return proteinG;
    }

    public void setProteinG(Double proteinG) {
        this.proteinG = proteinG;
    }

    public Double getFatG() {
        return fatG;
    }

    public void setFatG(Double fatG) {
        this.fatG = fatG;
    }

    public Double getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(Double carbsG) {
        this.carbsG = carbsG;
    }

    public Double getFiberG() {
        return fiberG;
    }

    public void setFiberG(Double fiberG) {
        this.fiberG = fiberG;
    }

    public Double getPortionAmount() {
        return portionAmount;
    }

    public void setPortionAmount(Double portionAmount) {
        this.portionAmount = portionAmount;
    }

    public String getPortionUnit() {
        return portionUnit;
    }

    public void setPortionUnit(String portionUnit) {
        this.portionUnit = portionUnit;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
}
