package com.foodprint.food.dto;

import java.util.UUID;

public class MealItemDTO {
    private String nameEn;
    private String nameZh;
    private Double calories;
    @com.fasterxml.jackson.annotation.JsonProperty("proteinG")
    private Double proteinG;
    @com.fasterxml.jackson.annotation.JsonProperty("fatG")
    private Double fatG;
    @com.fasterxml.jackson.annotation.JsonProperty("carbsG")
    private Double carbsG;
    @com.fasterxml.jackson.annotation.JsonProperty("fiberG")
    private Double fiberG;
    private Double portionAmount;
    private String portionUnit;
    private String dataSource;

    public MealItemDTO() {}

    // Getters and Setters
    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameZh() { return nameZh; }
    public void setNameZh(String nameZh) { this.nameZh = nameZh; }

    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }

    public Double getProteinG() { return proteinG; }
    public void setProteinG(Double proteinG) { this.proteinG = proteinG; }

    public Double getFatG() { return fatG; }
    public void setFatG(Double fatG) { this.fatG = fatG; }

    public Double getCarbsG() { return carbsG; }
    public void setCarbsG(Double carbsG) { this.carbsG = carbsG; }

    public Double getFiberG() { return fiberG; }
    public void setFiberG(Double fiberG) { this.fiberG = fiberG; }

    public Double getPortionAmount() { return portionAmount; }
    public void setPortionAmount(Double portionAmount) { this.portionAmount = portionAmount; }

    public String getPortionUnit() { return portionUnit; }
    public void setPortionUnit(String portionUnit) { this.portionUnit = portionUnit; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
}
