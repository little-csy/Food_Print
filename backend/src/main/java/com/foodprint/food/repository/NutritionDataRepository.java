package com.foodprint.food.repository;

// TODO [Engineer B]: Implement NutritionDataRepository

import com.foodprint.food.entity.NutritionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NutritionDataRepository extends JpaRepository<NutritionData, UUID> {

    // Find all nutrition items belonging to a food log
    List<NutritionData> findByFoodLogId(UUID foodLogId);

}
