package com.foodprint.exercise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.exercise.dto.ExerciseTodayResponse;
import com.foodprint.exercise.service.ExerciseLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Isolated web-layer tests for ExerciseController.
 *
 * Security filters are disabled via @AutoConfigureMockMvc(addFilters = false).
 */
@WebMvcTest(ExerciseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExerciseLogService exerciseLogService;

    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // POST /api/exercise-logs
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/exercise-logs – 200 with updated today summary")
    void logExercise_success() throws Exception {
        ExerciseTodayResponse.ExerciseEntry entry =
                new ExerciseTodayResponse.ExerciseEntry(
                        UUID.randomUUID(), "Running", 300.0, 30, null, null);
        ExerciseTodayResponse mockResponse =
                new ExerciseTodayResponse(300.0, 30, List.of(entry));
        when(exerciseLogService.logExercise(any())).thenReturn(mockResponse);

        String body = """
                {
                  "activityType": "Running",
                  "caloriesBurned": 300.0,
                  "durationMin": 30
                }
                """;

        mockMvc.perform(post("/api/exercise-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Exercise logged!"))
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(300.0))
                .andExpect(jsonPath("$.data.totalDurationMin").value(30))
                .andExpect(jsonPath("$.data.exercises.length()").value(1))
                .andExpect(jsonPath("$.data.exercises[0].activityType").value("Running"))
                .andExpect(jsonPath("$.data.exercises[0].caloriesBurned").value(300.0));
    }

    @Test
    @DisplayName("POST /api/exercise-logs – 400 when service throws for blank activityType")
    void logExercise_blankActivityType_returns400() throws Exception {
        when(exerciseLogService.logExercise(any()))
                .thenThrow(new IllegalArgumentException("Activity type is required."));

        String body = """
                { "activityType": "", "caloriesBurned": 300.0 }
                """;

        mockMvc.perform(post("/api/exercise-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Activity type is required."));
    }

    @Test
    @DisplayName("POST /api/exercise-logs – 400 when service throws for zero calories")
    void logExercise_zeroCalories_returns400() throws Exception {
        when(exerciseLogService.logExercise(any()))
                .thenThrow(new IllegalArgumentException("Calories burned must be greater than 0."));

        String body = """
                { "activityType": "Walking", "caloriesBurned": 0.0 }
                """;

        mockMvc.perform(post("/api/exercise-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/exercise-logs – durationMin is optional; request succeeds without it")
    void logExercise_withoutDuration_succeeds() throws Exception {
        ExerciseTodayResponse mockResponse = new ExerciseTodayResponse(150.0, 0, List.of());
        when(exerciseLogService.logExercise(any())).thenReturn(mockResponse);

        String body = """
                { "activityType": "Yoga", "caloriesBurned": 150.0 }
                """;

        mockMvc.perform(post("/api/exercise-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(150.0));
    }

    // -----------------------------------------------------------------------
    // GET /api/exercise-logs/today
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/exercise-logs/today – 200 with aggregated session data")
    void getTodayExercise_success() throws Exception {
        ExerciseTodayResponse.ExerciseEntry e1 =
                new ExerciseTodayResponse.ExerciseEntry(
                        UUID.randomUUID(), "Running",  300.0, 30, null, null);
        ExerciseTodayResponse.ExerciseEntry e2 =
                new ExerciseTodayResponse.ExerciseEntry(
                        UUID.randomUUID(), "Cycling",  200.0, 20, null, null);
        ExerciseTodayResponse mockResponse =
                new ExerciseTodayResponse(500.0, 50, List.of(e1, e2));

        when(exerciseLogService.getTodayExercise()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/exercise-logs/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(500.0))
                .andExpect(jsonPath("$.data.totalDurationMin").value(50))
                .andExpect(jsonPath("$.data.exercises.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/exercise-logs/today – 200 with empty list when no sessions today")
    void getTodayExercise_noSessions() throws Exception {
        ExerciseTodayResponse mockResponse = new ExerciseTodayResponse(0.0, 0, List.of());
        when(exerciseLogService.getTodayExercise()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/exercise-logs/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCaloriesBurned").value(0.0))
                .andExpect(jsonPath("$.data.exercises").isEmpty());
    }
}
