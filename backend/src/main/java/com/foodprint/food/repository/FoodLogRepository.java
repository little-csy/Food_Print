package com.foodprint.food.repository;

// TODO [Engineer B]: Implement FoodLogRepository

import com.foodprint.food.entity.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, UUID> {

    // Find food logs for a user on a specific date
    List<FoodLog> findByUserIdAndLogDate(UUID userId, LocalDate date);

    // Find food logs for a user within a date range
    List<FoodLog> findByUserIdAndLogDateBetween(
            UUID userId,
            LocalDate start,
            LocalDate end
    );

    // Count distinct active days for a user
    @Query("SELECT COUNT(DISTINCT f.logDate) FROM FoodLog f WHERE f.userId = :userId")
    long countDistinctLogDateByUserId(@Param("userId") UUID userId);
}
