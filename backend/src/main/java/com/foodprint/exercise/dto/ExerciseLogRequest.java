package com.foodprint.exercise.dto;

/** Request body for logging an exercise session. */
public class ExerciseLogRequest {

    /** Activity type, e.g. "Running", "Cycling", "Swimming", "Yoga", "Walking" */
    private String activityType;

    /** Calories burned during this session */
    private double caloriesBurned;

    /** Duration in minutes (optional) */
    private Integer durationMin;

    private String note;

    public ExerciseLogRequest() {}

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public double getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(double caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
