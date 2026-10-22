package com.foodprint.challenge.dto;

import java.time.LocalDate;

/** Response DTO returned after a daily check-in attempt. */
public class CheckinResponse {

    private LocalDate date;
    private boolean passed;
    private double actualValue;
    private String note;
    private int completedDays;
    private int currentStreak;
    private boolean challengeCompleted;
    private String message;

    /**
     * Full snapshot of today's nutrition and activity data.
     * The frontend can use any of these values to display progress.
     */
    private NutritionSnapshot nutritionSnapshot;

    public CheckinResponse() {}

    public CheckinResponse(LocalDate date, boolean passed, double actualValue, String note,
                           int completedDays, int currentStreak,
                           boolean challengeCompleted, String message,
                           NutritionSnapshot nutritionSnapshot) {
        this.date = date;
        this.passed = passed;
        this.actualValue = actualValue;
        this.note = note;
        this.completedDays = completedDays;
        this.currentStreak = currentStreak;
        this.challengeCompleted = challengeCompleted;
        this.message = message;
        this.nutritionSnapshot = nutritionSnapshot;
    }

    /** Legacy constructor without nutritionSnapshot for backward compatibility */
    public CheckinResponse(LocalDate date, boolean passed, double actualValue, String note,
                           int completedDays, int currentStreak,
                           boolean challengeCompleted, String message) {
        this(date, passed, actualValue, note, completedDays, currentStreak,
                challengeCompleted, message, null);
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public double getActualValue() { return actualValue; }
    public void setActualValue(double actualValue) { this.actualValue = actualValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public boolean isChallengeCompleted() { return challengeCompleted; }
    public void setChallengeCompleted(boolean challengeCompleted) { this.challengeCompleted = challengeCompleted; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NutritionSnapshot getNutritionSnapshot() { return nutritionSnapshot; }
    public void setNutritionSnapshot(NutritionSnapshot nutritionSnapshot) { this.nutritionSnapshot = nutritionSnapshot; }
}
