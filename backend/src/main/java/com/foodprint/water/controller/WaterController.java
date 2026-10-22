package com.foodprint.water.controller;

import com.foodprint.util.ApiResponse;
import com.foodprint.water.dto.WaterLogRequest;
import com.foodprint.water.dto.WaterTodayResponse;
import com.foodprint.water.service.WaterLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Water intake logging API.
 *
 * POST  /api/water-logs        — log a water intake event (amountMl required)
 * GET   /api/water-logs/today  — today's summary: total ml, cups, goal progress, log history
 */
@RestController
@RequestMapping("/api/water-logs")
public class WaterController {

    @Autowired
    private WaterLogService waterLogService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
    }

    /**
     * Log water intake for today.
     * Body: { "amountMl": 500, "note": "morning glass" }
     * Returns updated today's summary.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WaterTodayResponse>> logWater(
            @RequestBody WaterLogRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Water logged!", waterLogService.logWater(request)));
    }

    /**
     * Get today's water intake summary.
     * Returns totalMl, totalCups, goalMl (2000), percentAchieved, and individual log entries.
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<WaterTodayResponse>> getTodayIntake() {
        return ResponseEntity.ok(ApiResponse.success(waterLogService.getTodayIntake()));
    }
}
