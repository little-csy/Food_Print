package com.foodprint.food.controller;

// TODO [Engineer B]: Implement FoodController

import com.foodprint.food.dto.CreateFoodLogRequest;
import com.foodprint.food.dto.ManualMealLogRequest;
import com.foodprint.food.dto.TodayMealResponse;
import com.foodprint.food.dto.WeeklyOverviewResponse;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.service.FoodLoggingService;
import com.foodprint.util.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/food-logs")
public class FoodController {

    private final FoodLoggingService foodLoggingService;

    public FoodController(FoodLoggingService foodLoggingService) {
        this.foodLoggingService = foodLoggingService;
    }

    /**
     * Create a food log manually (Pure CRUD, no AI).
     */
    @PostMapping("/manual")
    public ApiResponse<FoodLog> createManualLog(
            @RequestBody ManualMealLogRequest request
    ) {
        FoodLog savedLog = foodLoggingService.saveManualMealLog(request);
        return ApiResponse.success(savedLog);
    }

    /**
     * Create a food log via AI (Input to AI Analysis).
     */
    @PostMapping
    public ApiResponse<FoodLog> createFoodLog(
            @RequestBody CreateFoodLogRequest request
    ) {
        LocalDate logDate = null;
        if (request.getDate() != null && !request.getDate().isBlank()) {
            logDate = LocalDate.parse(request.getDate());
        }

        FoodLog savedLog = foodLoggingService.createFoodLog(
                request.getText(),
                request.getImageUrl(),
                logDate
        );

        return ApiResponse.success(savedLog);
    }

    @GetMapping("/weekly-overview")
    public ApiResponse<WeeklyOverviewResponse> getWeeklyOverview(
            @RequestParam(required = false) String endDate
    ) {
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate) : LocalDate.now();
        WeeklyOverviewResponse overview = foodLoggingService.getWeeklyOverview(end);
        return ApiResponse.success(overview);
    }

    /**
     * Get a simplified list of meals logged today for the home screen.
     */
    @GetMapping("/today")
    public ApiResponse<List<TodayMealResponse>> getTodayMeals() {
        return ApiResponse.success(foodLoggingService.getTodayMeals());
    }
}
