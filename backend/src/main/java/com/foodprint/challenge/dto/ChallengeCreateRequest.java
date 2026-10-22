package com.foodprint.challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for creating a new challenge template.
 * All fields except badgeIcon and targetValue are required.
 */
public class ChallengeCreateRequest {

    private String title;
    private String description;
    private String type;

    @JsonProperty("duration_days")
    private int durationDays;

    @JsonProperty("target_value")
    private double targetValue = 0.0;

    @JsonProperty("badge_icon")
    private String badgeIcon = "🏆";

    public ChallengeCreateRequest() {}

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
}
