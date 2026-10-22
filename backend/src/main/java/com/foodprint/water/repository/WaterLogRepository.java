package com.foodprint.water.repository;

import com.foodprint.water.entity.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, UUID> {

    List<WaterLog> findByUserIdAndLogDateOrderByCreatedAtAsc(UUID userId, LocalDate date);

    @Query("SELECT COALESCE(SUM(w.amountMl), 0) FROM WaterLog w WHERE w.userId = :userId AND w.logDate = :date")
    double sumAmountMlByUserIdAndLogDate(@Param("userId") UUID userId, @Param("date") LocalDate date);
}
