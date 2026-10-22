package com.foodprint.exercise.repository;

import com.foodprint.exercise.entity.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, UUID> {

    List<ExerciseLog> findByUserIdAndLogDateOrderByCreatedAtAsc(UUID userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(e.caloriesBurned), 0) FROM ExerciseLog e WHERE e.userId = :userId AND e.logDate = :date")
    double sumCaloriesBurnedByUserIdAndLogDate(@Param("userId") UUID userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.durationMin), 0L) FROM ExerciseLog e WHERE e.userId = :userId AND e.logDate = :date")
    long sumDurationMinByUserIdAndLogDate(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
