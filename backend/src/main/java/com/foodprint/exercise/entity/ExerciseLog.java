package com.foodprint.exercise.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Records a single exercise/activity session for a user on a given day. */
@Entity
@Table(name = "exercise_logs")
public class ExerciseLog {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "profile_id", nullable = false)
    private UUID userId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    /**
     * Free-form activity type, e.g. "Running", "Cycling", "Yoga", "Walking".
     * Frontend can show suggestions but any text is accepted.
     */
    @Column(name = "activity_type", nullable = false, length = 100)
    private String activityType;

    /** Estimated calories burned during this session */
    @Column(name = "calories_burned", nullable = false)
    private double caloriesBurned;

    /** Duration of the session in minutes */
    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(length = 300)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public ExerciseLog() {}

    public ExerciseLog(UUID userId, LocalDate logDate, String activityType,
                       double caloriesBurned, Integer durationMin, String note) {
        this.userId = userId;
        this.logDate = logDate;
        this.activityType = activityType;
        this.caloriesBurned = caloriesBurned;
        this.durationMin = durationMin;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
