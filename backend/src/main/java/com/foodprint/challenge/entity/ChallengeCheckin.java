package com.foodprint.challenge.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Daily check-in record for a user challenge.
 * One check-in per (userChallengeId, checkinDate) is enforced by the unique constraint.
 */
@Entity
@Table(name = "challenge_checkins",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_challenge_id", "checkin_date"}))
public class ChallengeCheckin {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_challenge_id", nullable = false)
    private UUID userChallengeId;

    /** Matches Supabase Profile connection */
    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    /** Stored redundantly for efficient per-user queries */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    /** Whether the user met the challenge criteria on this day */
    @Column(nullable = false)
    private boolean passed;

    /** The actual measured value for the day (calories / protein / carbs / log count) */
    @Column(name = "actual_value")
    private double actualValue;

    /** Human-readable summary shown to the user, e.g. "Today's protein: 62g ✅ Goal met!" */
    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ---------- Constructors ----------

    public ChallengeCheckin() {}

    public ChallengeCheckin(UUID userChallengeId, UUID userId, LocalDate checkinDate,
                            boolean passed, double actualValue, String note) {
        this.userChallengeId = userChallengeId;
        this.userId = userId;
        this.profileId = userId; // In this system, userId and profileId are usually identical
        this.checkinDate = checkinDate;
        this.passed = passed;
        this.actualValue = actualValue;
        this.note = note;
        // Set createdAt eagerly — @PrePersist may not fire when merge() is used for new entities
        // with a pre-assigned UUID (Spring Data JPA isNew() returns false when id != null).
        this.createdAt = LocalDateTime.now();
    }

    // ---------- Getters & Setters ----------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserChallengeId() { return userChallengeId; }
    public void setUserChallengeId(UUID userChallengeId) { this.userChallengeId = userChallengeId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getProfileId() { return profileId; }
    public void setProfileId(UUID profileId) { this.profileId = profileId; }

    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public double getActualValue() { return actualValue; }
    public void setActualValue(double actualValue) { this.actualValue = actualValue; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
