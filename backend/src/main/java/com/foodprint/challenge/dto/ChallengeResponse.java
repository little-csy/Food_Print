package com.foodprint.challenge.dto;

import java.util.UUID;

/** Response DTO for a system-defined challenge template. */
public class ChallengeResponse {

    private UUID id;
    private String title;
    private String description;
    private String type;
    private int durationDays;
    private double targetValue;
    private String badgeIcon;

    public ChallengeResponse() {}

    public ChallengeResponse(UUID id, String title, String description, String type,
                              int durationDays, double targetValue, String badgeIcon) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.durationDays = durationDays;
        this.targetValue = targetValue;
        this.badgeIcon = badgeIcon;
    }

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
}
