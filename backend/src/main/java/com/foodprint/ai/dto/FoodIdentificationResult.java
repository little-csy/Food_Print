package com.foodprint.ai.dto;

import java.util.List;

/**
 * Result of AI food identification from image or text.
 */
public class FoodIdentificationResult {
    private List<FoodItem> foods;
    private String mealDescription;
    private double confidence;

    public FoodIdentificationResult() {
    }

    public FoodIdentificationResult(List<FoodItem> foods, String mealDescription, double confidence) {
        this.foods = foods;
        this.mealDescription = mealDescription;
        this.confidence = confidence;
    }

    public List<FoodItem> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodItem> foods) {
        this.foods = foods;
    }

    public String getMealDescription() {
        return mealDescription;
    }

    public void setMealDescription(String mealDescription) {
        this.mealDescription = mealDescription;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    /**
     * A single food item identified by AI.
     */
    public static class FoodItem {
        private String nameEn;
        private String nameZh;
        private double portionAmount;
        private String portionUnit;
        private String category;
        private double estimatedCalories;
        private double estimatedProteinG;
        private double estimatedFatG;
        private double estimatedCarbsG;
        private double estimatedFiberG;

        public FoodItem() {
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

        public double getPortionAmount() {
            return portionAmount;
        }

        public void setPortionAmount(double portionAmount) {
            this.portionAmount = portionAmount;
        }

        public String getPortionUnit() {
            return portionUnit;
        }

        public void setPortionUnit(String portionUnit) {
            this.portionUnit = portionUnit;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public double getEstimatedCalories() {
            return estimatedCalories;
        }

        public void setEstimatedCalories(double estimatedCalories) {
            this.estimatedCalories = estimatedCalories;
        }

        public double getEstimatedProteinG() {
            return estimatedProteinG;
        }

        public void setEstimatedProteinG(double estimatedProteinG) {
            this.estimatedProteinG = estimatedProteinG;
        }

        public double getEstimatedFatG() {
            return estimatedFatG;
        }

        public void setEstimatedFatG(double estimatedFatG) {
            this.estimatedFatG = estimatedFatG;
        }

        public double getEstimatedCarbsG() {
            return estimatedCarbsG;
        }

        public void setEstimatedCarbsG(double estimatedCarbsG) {
            this.estimatedCarbsG = estimatedCarbsG;
        }

        public double getEstimatedFiberG() {
            return estimatedFiberG;
        }

        public void setEstimatedFiberG(double estimatedFiberG) {
            this.estimatedFiberG = estimatedFiberG;
        }
    }
}
