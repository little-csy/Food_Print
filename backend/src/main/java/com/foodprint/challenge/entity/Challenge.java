package com.foodprint.challenge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Challenge template — system-defined, auto-seeded into the database on startup.
 * The type field determines the validation logic used during check-in.
 */
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    private UUID id = UUID.randomUUID();

    /** Display name, e.g. "Calorie Guardian" */
    @Column(nullable = false, length = 100)
    private String title;

    /** Full description shown to the user */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Challenge type — drives the check-in evaluation logic:
     * CALORIE_CONTROL / PROTEIN_CHAMPION / LOG_STREAK / LOW_CARB / LIGHT_EATER
     */
    @Column(nullable = false, length = 50)
    private String type;

    /** Number of days the challenge lasts */
    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    /**
     * Numeric threshold used during evaluation:
     *   CALORIE_CONTROL  -> 0    (uses user's personal calorie target)
     *   PROTEIN_CHAMPION -> 50   (minimum grams of protein per day)
     *   LOG_STREAK       -> 1    (minimum food log entries per day)
     *   LOW_CARB         -> 150  (maximum grams of carbs per day)
     *   LIGHT_EATER      -> 1500 (maximum kcal per day)
     *   DAILY_WATER      -> 2000 (minimum ml of water per day)
     *   CALORIE_BURN     -> 200  (minimum kcal burned via exercise per day)
     */
    @Column(name = "target_value", nullable = false)
    private double targetValue;

    /** Badge icon (emoji) shown in the UI */
    @Column(name = "badge_icon", length = 10)
    private String badgeIcon;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ---------- Constructors ----------

    public Challenge() {}

    public Challenge(UUID id, String title, String description, String type,
                     int durationDays, double targetValue, String badgeIcon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.durationDays = durationDays;
        this.targetValue = targetValue;
        this.badgeIcon = badgeIcon;
        // Set createdAt eagerly — @PrePersist may not fire when merge() is used for new entities
        // with a pre-assigned UUID (Spring Data JPA isNew() returns false when id != null).
        this.createdAt = LocalDateTime.now();
    }

    public Challenge(String title, String description, String type,
                     int durationDays, double targetValue, String badgeIcon) {
        this(UUID.randomUUID(), title, description, type, durationDays, targetValue, badgeIcon);
    }

    // ---------- Getters & Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public String getBadgeIcon() { return badgeIcon; }
    public void setBadgeIcon(String badgeIcon) { this.badgeIcon = badgeIcon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
