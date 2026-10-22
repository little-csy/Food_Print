package com.foodprint.challenge.dto;

/**
 * Read-only preview of today's metrics for an active challenge.
 * Returned by GET /api/challenges/my/{id}/today-status.
 * Does NOT create a check-in record — purely informational.
 */
public class TodayStatusResponse {

    /** Whether the user has already checked in today */
    private boolean checkedIn;

    /** Whether the user passed today (null if not yet checked in) */
    private Boolean passed;

    /** The relevant measured value for today (calories / protein / water ml / etc.) */
    private double currentValue;

    /** The target value the user needs to reach or stay within */
    private double targetValue;

    /** Human-readable summary of today's progress */
    private String summary;

    /** Days remaining until the challenge ends (inclusive of today) */
    private int daysRemaining;

    /** Current streak of consecutive passed days */
    private int currentStreak;
    private int progressPercent;

    /**
     * Full snapshot of today's nutrition and activity data.
     * The frontend can use any of these values to display progress.
     */
    private NutritionSnapshot nutritionSnapshot;

    public TodayStatusResponse() {}

    public TodayStatusResponse(boolean checkedIn, Boolean passed, double currentValue,
                                double targetValue, String summary,
                                int daysRemaining, int currentStreak, int progressPercent,
                                NutritionSnapshot nutritionSnapshot) {
        this.checkedIn = checkedIn;
        this.passed = passed;
        this.currentValue = currentValue;
        this.targetValue = targetValue;
        this.summary = summary;
        this.daysRemaining = daysRemaining;
        this.currentStreak = currentStreak;
        this.progressPercent = progressPercent;
        this.nutritionSnapshot = nutritionSnapshot;
    }

    /** Legacy constructor without nutritionSnapshot */
    public TodayStatusResponse(boolean checkedIn, Boolean passed, double currentValue,
                                double targetValue, String summary,
                                int daysRemaining, int currentStreak, int progressPercent) {
        this(checkedIn, passed, currentValue, targetValue, summary,
                daysRemaining, currentStreak, progressPercent, null);
    }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public NutritionSnapshot getNutritionSnapshot() { return nutritionSnapshot; }
    public void setNutritionSnapshot(NutritionSnapshot nutritionSnapshot) { this.nutritionSnapshot = nutritionSnapshot; }
}
