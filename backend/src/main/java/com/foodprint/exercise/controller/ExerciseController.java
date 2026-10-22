package com.foodprint.exercise.controller;

import com.foodprint.exercise.dto.ExerciseLogRequest;
import com.foodprint.exercise.dto.ExerciseTodayResponse;
import com.foodprint.exercise.service.ExerciseLogService;
import com.foodprint.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exercise / calorie-burn logging API.
 *
 * POST  /api/exercise-logs        — log an exercise session
 * GET   /api/exercise-logs/today  — today's summary: total calories burned, sessions
 */
@RestController
@RequestMapping("/api/exercise-logs")
public class ExerciseController {

    @Autowired
    private ExerciseLogService exerciseLogService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }

    /**
     * Log an exercise session.
     * Body: { "activityType": "Running", "caloriesBurned": 300, "durationMin": 30, "note": "..." }
     * Returns updated today's summary.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseTodayResponse>> logExercise(
            @RequestBody ExerciseLogRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Exercise logged!", exerciseLogService.logExercise(request)));
    }

    /**
     * Get today's exercise summary.
     * Returns totalCaloriesBurned, totalDurationMin, and individual session list.
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<ExerciseTodayResponse>> getTodayExercise() {
        return ResponseEntity.ok(ApiResponse.success(exerciseLogService.getTodayExercise()));
    }
}
