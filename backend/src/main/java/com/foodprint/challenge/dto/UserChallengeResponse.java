package com.foodprint.challenge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/** Response DTO for a user's participation record in a challenge, including progress. */
public class UserChallengeResponse {

    private UUID id;
    private UUID userId;
    private UUID profileId;

    /** The challenge template this record belongs to */
    private ChallengeResponse challenge;

    /** Status: IN_PROGRESS / COMPLETED / ABANDONED */
    private String status;

    private LocalDate startDate;
    private LocalDate endDate;

    /** Number of days the user met the challenge criteria */
    private int completedDays;

    /** Current consecutive streak of passed check-ins */
    private int currentStreak;

    /** Progress percentage (0-100), derived from completedDays / durationDays */
    private int progressPercent;

    /** Days left until the challenge ends (0 when on the last day, negative when expired) */
    private int daysRemaining;

    /** Whether the user has already checked in today (populated for IN_PROGRESS challenges) */
    private boolean todayCheckedIn;

    /** Whether today's check-in passed; null means not yet checked in today */
    private Boolean todayPassed;

    private LocalDateTime joinedAt;
    private LocalDateTime completedAt;

    /**
     * Check-in history sorted by date ascending.
     * Empty list on list endpoints; populated on the detail endpoint.
     */
    private List<CheckinResponse> checkins;

    public UserChallengeResponse() {}

    public UserChallengeResponse(UUID id, UUID userId, UUID profileId, ChallengeResponse challenge,
                                  String status, LocalDate startDate, LocalDate endDate,
                                  int completedDays, int currentStreak,
                                  LocalDateTime joinedAt, LocalDateTime completedAt,
                                  List<CheckinResponse> checkins) {
        this.id = id;
        this.userId = userId;
        this.profileId = profileId;
        this.challenge = challenge;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.completedDays = completedDays;
        this.currentStreak = currentStreak;
        this.progressPercent = (challenge != null && challenge.getDurationDays() > 0)
                ? (int) Math.min(100, completedDays * 100.0 / challenge.getDurationDays())
                : 0;
        this.daysRemaining = (endDate != null)
                ? (int) ChronoUnit.DAYS.between(LocalDate.now(), endDate)
                : 0;
        this.joinedAt = joinedAt;
        this.completedAt = completedAt;
        this.checkins = checkins;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public ChallengeResponse getChallenge() { return challenge; }
    public void setChallenge(ChallengeResponse challenge) { this.challenge = challenge; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public int getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(int daysRemaining) { this.daysRemaining = daysRemaining; }

    public boolean isTodayCheckedIn() { return todayCheckedIn; }
    public void setTodayCheckedIn(boolean todayCheckedIn) { this.todayCheckedIn = todayCheckedIn; }

    public Boolean getTodayPassed() { return todayPassed; }
    public void setTodayPassed(Boolean todayPassed) { this.todayPassed = todayPassed; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public List<CheckinResponse> getCheckins() { return checkins; }
    public void setCheckins(List<CheckinResponse> checkins) { this.checkins = checkins; }
}
