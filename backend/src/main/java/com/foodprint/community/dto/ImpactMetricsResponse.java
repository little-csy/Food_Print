package com.foodprint.community.dto;

public class ImpactMetricsResponse {
    private long totalUsers;
    private double totalWeightLostLbs;

    public ImpactMetricsResponse() {
    }

    public ImpactMetricsResponse(long totalUsers, double totalWeightLostLbs) {
        this.totalUsers = totalUsers;
        this.totalWeightLostLbs = totalWeightLostLbs;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public double getTotalWeightLostLbs() {
        return totalWeightLostLbs;
    }

    public void setTotalWeightLostLbs(double totalWeightLostLbs) {
        this.totalWeightLostLbs = totalWeightLostLbs;
    }
}
