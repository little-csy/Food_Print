package com.foodprint.ai.dto;

import java.util.List;

/**
 * Complete food analysis result returned to the client.
 */
public class FoodAnalysisResult {
    private List<FoodNutritionDetail> foods;
    private MealSummary summary;
    private double confidence;

    public FoodAnalysisResult() {
    }

    public FoodAnalysisResult(List<FoodNutritionDetail> foods, MealSummary summary, double confidence) {
        this.foods = foods;
        this.summary = summary;
        this.confidence = confidence;
    }

    public List<FoodNutritionDetail> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodNutritionDetail> foods) {
        this.foods = foods;
    }

    public MealSummary getSummary() {
        return summary;
    }

    public void setSummary(MealSummary summary) {
        this.summary = summary;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /**
     * Detailed nutrition info for a single food item, including data source.
     */
    public static class FoodNutritionDetail {
        private String nameEn;
        private String nameZh;
        private Double portionAmount;
        private String portionUnit;
        private Double calories;
        private Double proteinG;
        private Double fatG;
        private Double carbsG;
        private Double fiberG;
        private String dataSource; // "USDA" or "AI_ESTIMATE"
        private boolean flagged; // true if post-processing detected anomaly
        private String flagReason;

        public FoodNutritionDetail() {
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

        public String getDataSource() {
            return dataSource;
        }

        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }

        public boolean isFlagged() {
            return flagged;
        }

        public void setFlagged(boolean flagged) {
            this.flagged = flagged;
        }

        public String getFlagReason() {
            return flagReason;
        }

        public void setFlagReason(String flagReason) {
            this.flagReason = flagReason;
        }
    }

    /**
     * Aggregated nutrition for the entire meal.
     */
    public static class MealSummary {
        private Double totalCalories;
        private Double totalProteinG;
        private Double totalFatG;
        private Double totalCarbsG;
        private Double totalFiberG;
        private int foodCount;

        public MealSummary() {
        }

        public Double getTotalCalories() {
            return totalCalories;
        }

        public void setTotalCalories(Double totalCalories) {
            this.totalCalories = totalCalories;
        }

        public Double getTotalProteinG() {
            return totalProteinG;
        }

        public void setTotalProteinG(Double totalProteinG) {
            this.totalProteinG = totalProteinG;
        }

        public Double getTotalFatG() {
            return totalFatG;
        }

        public void setTotalFatG(Double totalFatG) {
            this.totalFatG = totalFatG;
        }

        public Double getTotalCarbsG() {
            return totalCarbsG;
        }

        public void setTotalCarbsG(Double totalCarbsG) {
            this.totalCarbsG = totalCarbsG;
        }

        public Double getTotalFiberG() {
            return totalFiberG;
        }

        public void setTotalFiberG(Double totalFiberG) {
            this.totalFiberG = totalFiberG;
        }

        public int getFoodCount() {
            return foodCount;
        }

        public void setFoodCount(int foodCount) {
            this.foodCount = foodCount;
        }
    }
}
