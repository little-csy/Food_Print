package com.foodprint.community.entity;

import java.util.UUID;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "topicName"}))
public class CommunitySubscription {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String topicName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public CommunitySubscription() {
    }

    public CommunitySubscription(UUID userId, String topicName) {
        this.userId = userId;
        this.topicName = topicName;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
