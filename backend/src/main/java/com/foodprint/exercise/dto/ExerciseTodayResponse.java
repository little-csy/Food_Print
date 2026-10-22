package com.foodprint.exercise.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Today's exercise summary with individual session entries. */
public class ExerciseTodayResponse {

    /** Total calories burned from all sessions today */
    private double totalCaloriesBurned;

    /** Total exercise time today in minutes */
    private int totalDurationMin;

    /** Individual exercise sessions logged today */
    private List<ExerciseEntry> exercises;

    public ExerciseTodayResponse() {}

    public ExerciseTodayResponse(double totalCaloriesBurned, int totalDurationMin,
                                  List<ExerciseEntry> exercises) {
        this.totalCaloriesBurned = totalCaloriesBurned;
        this.totalDurationMin = totalDurationMin;
        this.exercises = exercises;
    }

    public double getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(double totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }

    public int getTotalDurationMin() { return totalDurationMin; }
    public void setTotalDurationMin(int totalDurationMin) { this.totalDurationMin = totalDurationMin; }

    public List<ExerciseEntry> getExercises() { return exercises; }
    public void setExercises(List<ExerciseEntry> exercises) { this.exercises = exercises; }

    /** One exercise session entry. */
    public static class ExerciseEntry {
        private UUID id;
        private String activityType;
        private double caloriesBurned;
        private Integer durationMin;
        private String note;
        private LocalDateTime loggedAt;

        public ExerciseEntry() {}

        public ExerciseEntry(UUID id, String activityType, double caloriesBurned,
                              Integer durationMin, String note, LocalDateTime loggedAt) {
            this.id = id;
            this.activityType = activityType;
            this.caloriesBurned = caloriesBurned;
            this.durationMin = durationMin;
            this.note = note;
            this.loggedAt = loggedAt;
        }

        public UUID getId() { return id; }
        public String getActivityType() { return activityType; }
        public double getCaloriesBurned() { return caloriesBurned; }
        public Integer getDurationMin() { return durationMin; }
        public String getNote() { return note; }
        public LocalDateTime getLoggedAt() { return loggedAt; }
    }
}
