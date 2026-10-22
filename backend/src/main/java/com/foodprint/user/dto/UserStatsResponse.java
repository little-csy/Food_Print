package com.foodprint.user.dto;

public class UserStatsResponse {
    private int achievements;
    private int goalsMet;
    private long daysActive;

    public UserStatsResponse() {
    }

    public UserStatsResponse(int achievements, int goalsMet, long daysActive) {
        this.achievements = achievements;
        this.goalsMet = goalsMet;
        this.daysActive = daysActive;
    }

    public int getAchievements() {
        return achievements;
    }

    public void setAchievements(int achievements) {
        this.achievements = achievements;
    }

    public int getGoalsMet() {
        return goalsMet;
    }

    public void setGoalsMet(int goalsMet) {
        this.goalsMet = goalsMet;
    }

    public long getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(long daysActive) {
        this.daysActive = daysActive;
    }
}
