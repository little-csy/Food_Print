package com.foodprint.challenge.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records a user's participation in a challenge.
 *
 * A user may only have one IN_PROGRESS record per challenge at a time —
 * enforced by the service layer via existsByUserIdAndChallengeIdAndStatus().
 * Completed or abandoned runs are retained so users can see their history
 * and re-join the same challenge any number of times.
 */
@Entity
@Table(name = "user_challenges")
public class UserChallenge {

    @Id
    private UUID id = UUID.randomUUID();

    /** Matches Supabase Profile connection */
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "challenge_id", nullable = false)
    private UUID challengeId;

    /** Status: IN_PROGRESS / COMPLETED / ABANDONED */
    @Column(nullable = false, length = 20)
    private String status;

    /** The date the user joined the challenge */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Last valid check-in date = startDate + durationDays - 1 */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** Total number of days the user met the challenge criteria (passed=true) */
    @Column(name = "completed_days", nullable = false)
    private int completedDays = 0;

    /** Redundant progress field used by some DB views/frontend templates */
    @Column(name = "progress", nullable = false)
    private int progress = 0;

    /** Current consecutive streak of passed check-ins; resets to 0 on a missed day */
    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /** Set when status transitions to COMPLETED */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    // ---------- Constructors ----------

    public UserChallenge() {}

    public UserChallenge(UUID userId, UUID challengeId, LocalDate startDate, LocalDate endDate) {
        this.userId = userId;
        this.profileId = userId; // In this system, userId and profileId are usually identical
        this.challengeId = challengeId;
        this.status = "IN_PROGRESS";
        this.startDate = startDate;
        this.endDate = endDate;
        this.completedDays = 0;
        this.progress = 0;
        this.currentStreak = 0;
        // Set joinedAt eagerly — @PrePersist may not fire when merge() is used for new entities
        // with a pre-assigned UUID (Spring Data JPA isNew() returns false when id != null).
        this.joinedAt = LocalDateTime.now();
    }

    // ---------- Getters & Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public UUID getChallengeId() { return challengeId; }
    public void setChallengeId(UUID challengeId) { this.challengeId = challengeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) {
        this.completedDays = completedDays;
        this.progress = completedDays; // Keep them in sync
    }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
