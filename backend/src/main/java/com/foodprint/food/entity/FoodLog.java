package com.foodprint.food.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "food_logs")
public class FoodLog {

    @Id
    private UUID id = UUID.randomUUID();

    // Supabase Auth user id
    @Column(name = "profile_id", nullable = false)
    private UUID userId;

    // Record title (optional, e.g., "Big Lunch")
    @Column(name = "title")
    private String title;

    // The date this food log belongs to
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    // Specific time of the meal (optional)
    @Column(name = "meal_time")
    private LocalDateTime mealTime;

    // Image URL returned from Supabase Storage
    @Column(name = "image_url")
    private String imageUrl;

    // Original user text input
    @Column(name = "original_text")
    private String originalText;

    // Confidence score returned by AI analysis
    @Column(name = "ai_confidence")
    private Double aiConfidence;

    // Record creation time
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public FoodLog() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LocalDateTime getMealTime() {
        return mealTime;
    }

    public void setMealTime(LocalDateTime mealTime) {
        this.mealTime = mealTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public Double getAiConfidence() {
        return aiConfidence;
    }

    public void setAiConfidence(Double aiConfidence) {
        this.aiConfidence = aiConfidence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.logDate == null) {
            this.logDate = LocalDate.now();
        }
    }
}
