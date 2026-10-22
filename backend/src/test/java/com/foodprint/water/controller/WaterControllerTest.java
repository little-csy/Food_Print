package com.foodprint.water.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.water.dto.WaterTodayResponse;
import com.foodprint.water.service.WaterLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Isolated web-layer tests for WaterController.
 *
 * Security filters are disabled via @AutoConfigureMockMvc(addFilters = false)
 * because authentication is handled by Supabase JWT — not under test here.
 */
@WebMvcTest(WaterController.class)
@AutoConfigureMockMvc(addFilters = false)
class WaterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaterLogService waterLogService;

    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // POST /api/water-logs
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/water-logs – 200 with updated today summary")
    void logWater_success() throws Exception {
        WaterTodayResponse mockResponse = new WaterTodayResponse(500.0, 2000.0, List.of());
        when(waterLogService.logWater(any())).thenReturn(mockResponse);

        String body = """
                { "amountMl": 500.0, "note": "morning glass" }
                """;

        mockMvc.perform(post("/api/water-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Water logged!"))
                .andExpect(jsonPath("$.data.totalMl").value(500.0))
                .andExpect(jsonPath("$.data.goalMl").value(2000.0))
                .andExpect(jsonPath("$.data.percentAchieved").value(25));
    }

    @Test
    @DisplayName("POST /api/water-logs – 400 when service throws IllegalArgumentException")
    void logWater_invalidAmount_returns400() throws Exception {
        when(waterLogService.logWater(any()))
                .thenThrow(new IllegalArgumentException("Amount must be greater than 0 ml."));

        String body = """
                { "amountMl": 0.0 }
                """;

        mockMvc.perform(post("/api/water-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Amount must be greater than 0 ml."));
    }

    @Test
    @DisplayName("POST /api/water-logs – note field is optional")
    void logWater_withoutNote_succeeds() throws Exception {
        WaterTodayResponse mockResponse = new WaterTodayResponse(237.0, 2000.0, List.of());
        when(waterLogService.logWater(any())).thenReturn(mockResponse);

        String body = """
                { "amountMl": 237.0 }
                """;

        mockMvc.perform(post("/api/water-logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalMl").value(237.0));
    }

    // -----------------------------------------------------------------------
    // GET /api/water-logs/today
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/water-logs/today – 200 with aggregated summary")
    void getTodayIntake_success() throws Exception {
        WaterTodayResponse.WaterLogEntry entry =
                new WaterTodayResponse.WaterLogEntry(
                        java.util.UUID.randomUUID(), 500.0, "lunch", null);
        WaterTodayResponse mockResponse = new WaterTodayResponse(1500.0, 2000.0, List.of(entry));

        when(waterLogService.getTodayIntake()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/water-logs/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalMl").value(1500.0))
                .andExpect(jsonPath("$.data.goalMl").value(2000.0))
                .andExpect(jsonPath("$.data.percentAchieved").value(75))
                .andExpect(jsonPath("$.data.logs").isArray())
                .andExpect(jsonPath("$.data.logs.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/water-logs/today – 200 with empty log list when nothing drunk yet")
    void getTodayIntake_nothingLogged() throws Exception {
        WaterTodayResponse mockResponse = new WaterTodayResponse(0.0, 2000.0, List.of());
        when(waterLogService.getTodayIntake()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/water-logs/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalMl").value(0.0))
                .andExpect(jsonPath("$.data.percentAchieved").value(0))
                .andExpect(jsonPath("$.data.logs").isEmpty());
    }
}
