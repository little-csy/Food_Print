package com.foodprint.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id; // UUID from Supabase Auth JWT subject

    private String email;
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    private Integer age;
    private String gender;
    private String goal;

    @Column(name = "daily_calorie_target")
    private Double dailyCalorieTarget;

    @Column(name = "dietary_preference")
    private String dietaryPreference;

    @Column(name = "created_at", updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}
