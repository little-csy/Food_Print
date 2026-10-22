package com.foodprint.water.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Today's water intake summary with individual log entries. */
public class WaterTodayResponse {

    /** Total water consumed today in millilitres */
    private double totalMl;

    /** Total in cups (1 cup = 237 ml), rounded to 1 decimal */
    private double totalCups;

    /** Daily hydration goal in ml (default 2000 ml = ~8 cups) */
    private double goalMl;

    /** Percentage of goal achieved (0-100+) */
    private int percentAchieved;

    /** Individual water log entries for today */
    private List<WaterLogEntry> logs;

    public WaterTodayResponse() {}

    public WaterTodayResponse(double totalMl, double goalMl, List<WaterLogEntry> logs) {
        this.totalMl = totalMl;
        this.totalCups = Math.round(totalMl / 237.0 * 10.0) / 10.0;
        this.goalMl = goalMl;
        this.percentAchieved = (int) Math.min(100, Math.round(totalMl / goalMl * 100));
        this.logs = logs;
    }

    public double getTotalMl() { return totalMl; }
    public void setTotalMl(double totalMl) { this.totalMl = totalMl; }

    public double getTotalCups() { return totalCups; }
    public void setTotalCups(double totalCups) { this.totalCups = totalCups; }

    public double getGoalMl() { return goalMl; }
    public void setGoalMl(double goalMl) { this.goalMl = goalMl; }

    public int getPercentAchieved() { return percentAchieved; }
    public void setPercentAchieved(int percentAchieved) { this.percentAchieved = percentAchieved; }

    public List<WaterLogEntry> getLogs() { return logs; }
    public void setLogs(List<WaterLogEntry> logs) { this.logs = logs; }

    /** Individual entry inside the today response. */
    public static class WaterLogEntry {
        private UUID id;
        private double amountMl;
        private String note;
        private LocalDateTime loggedAt;

        public WaterLogEntry() {}

        public WaterLogEntry(UUID id, double amountMl, String note, LocalDateTime loggedAt) {
            this.id = id;
            this.amountMl = amountMl;
            this.note = note;
            this.loggedAt = loggedAt;
        }

        public UUID getId() { return id; }
        public double getAmountMl() { return amountMl; }
        public String getNote() { return note; }
        public LocalDateTime getLoggedAt() { return loggedAt; }
    }
}
