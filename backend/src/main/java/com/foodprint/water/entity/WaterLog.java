package com.foodprint.water.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/** Records a single water intake event for a user on a given day. */
@Entity
@Table(name = "water_logs")
public class WaterLog {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "profile_id", nullable = false)
    private UUID userId;

    /** Date the water was consumed (defaults to today) */
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    /** Amount consumed in millilitres (1 cup ≈ 237 ml) */
    @Column(name = "amount_ml", nullable = false)
    private double amountMl;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public WaterLog() {}

    public WaterLog(UUID userId, LocalDate logDate, double amountMl, String note) {
        this.userId = userId;
        this.logDate = logDate;
        this.amountMl = amountMl;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public double getAmountMl() { return amountMl; }
    public void setAmountMl(double amountMl) { this.amountMl = amountMl; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
