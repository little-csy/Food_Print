package com.foodprint.water.service;

import com.foodprint.water.dto.WaterLogRequest;
import com.foodprint.water.dto.WaterTodayResponse;
import com.foodprint.water.entity.WaterLog;
import com.foodprint.water.repository.WaterLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WaterLogService.
 *
 * Covers: logging water intake, today's summary aggregation, goal percentage
 * calculation, cups conversion, validation guards, and the internal query
 * method used by ChallengeService.
 */
@ExtendWith(MockitoExtension.class)
class WaterLogServiceTest {

    @Mock
    private WaterLogRepository waterLogRepository;

    @InjectMocks
    private WaterLogService waterLogService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockAuth();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuth() {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(userId.toString());
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // -----------------------------------------------------------------------
    // logWater
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("logWater – saves the entry and returns the updated today summary")
    void logWater_success() {
        WaterLogRequest request = new WaterLogRequest();
        request.setAmountMl(500.0);
        request.setNote("morning glass");

        // After save, the today-summary query returns 500 ml total
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(500.0);

        WaterLog savedLog = new WaterLog(userId, LocalDate.now(), 500.0, "morning glass");
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(savedLog));

        WaterTodayResponse response = waterLogService.logWater(request);

        assertNotNull(response);
        assertEquals(500.0, response.getTotalMl());
        assertEquals(2000.0, response.getGoalMl());
        assertEquals(25, response.getPercentAchieved()); // 500/2000*100 = 25%
        assertEquals(1, response.getLogs().size());

        // Verify the entity was persisted exactly once
        verify(waterLogRepository, times(1)).save(any(WaterLog.class));
    }

    @Test
    @DisplayName("logWater – throws IllegalArgumentException when amountMl is zero")
    void logWater_throwsOnZeroAmount() {
        WaterLogRequest request = new WaterLogRequest();
        request.setAmountMl(0.0);

        assertThrows(IllegalArgumentException.class, () -> waterLogService.logWater(request));
        verify(waterLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("logWater – throws IllegalArgumentException when amountMl is negative")
    void logWater_throwsOnNegativeAmount() {
        WaterLogRequest request = new WaterLogRequest();
        request.setAmountMl(-250.0);

        assertThrows(IllegalArgumentException.class, () -> waterLogService.logWater(request));
        verify(waterLogRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getTodayIntake
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTodayIntake – returns zero summary when no water logged today")
    void getTodayIntake_noLogs() {
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(0.0);
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        WaterTodayResponse response = waterLogService.getTodayIntake();

        assertNotNull(response);
        assertEquals(0.0, response.getTotalMl());
        assertEquals(0.0, response.getTotalCups());
        assertEquals(0, response.getPercentAchieved());
        assertTrue(response.getLogs().isEmpty());
    }

    @Test
    @DisplayName("getTodayIntake – aggregates multiple log entries correctly")
    void getTodayIntake_multipleLogs() {
        // Three logs: 500 + 750 + 250 = 1500 ml
        WaterLog log1 = new WaterLog(userId, LocalDate.now(), 500.0, null);
        WaterLog log2 = new WaterLog(userId, LocalDate.now(), 750.0, "lunch");
        WaterLog log3 = new WaterLog(userId, LocalDate.now(), 250.0, null);

        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(1500.0);
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(log1, log2, log3));

        WaterTodayResponse response = waterLogService.getTodayIntake();

        assertEquals(1500.0, response.getTotalMl());
        assertEquals(75, response.getPercentAchieved()); // 1500/2000*100 = 75%
        assertEquals(3, response.getLogs().size());
    }

    @Test
    @DisplayName("getTodayIntake – percentAchieved caps at 100 when total exceeds goal")
    void getTodayIntake_percentCapsAt100() {
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(3000.0); // 150% of goal
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        WaterTodayResponse response = waterLogService.getTodayIntake();

        assertEquals(100, response.getPercentAchieved(), "percentAchieved must not exceed 100");
    }

    @Test
    @DisplayName("getTodayIntake – cups conversion is 1.0 cup for exactly 237 ml")
    void getTodayIntake_cupsConversion() {
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(237.0);
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        WaterTodayResponse response = waterLogService.getTodayIntake();

        assertEquals(1.0, response.getTotalCups(), 0.01, "237 ml should equal 1.0 cup");
    }

    @Test
    @DisplayName("getTodayIntake – exactly at goal (2000 ml) gives 100% achieved")
    void getTodayIntake_exactlyAtGoal() {
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(2000.0);
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        WaterTodayResponse response = waterLogService.getTodayIntake();

        assertEquals(100, response.getPercentAchieved());
    }

    // -----------------------------------------------------------------------
    // getTodayTotalMl (used by ChallengeService for evaluation)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTodayTotalMl – calls repository with the supplied userId, NOT the auth context")
    void getTodayTotalMl_usesSuppliedUserId() {
        UUID differentUserId = UUID.randomUUID(); // not the auth context userId
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(differentUserId), any(LocalDate.class)))
                .thenReturn(1800.0);

        double result = waterLogService.getTodayTotalMl(differentUserId);

        assertEquals(1800.0, result);
        verify(waterLogRepository).sumAmountMlByUserIdAndLogDate(eq(differentUserId), any(LocalDate.class));
        // Should NOT use the auth context userId
        verify(waterLogRepository, never())
                .sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class));
    }

    @Test
    @DisplayName("getTodayTotalMl – returns 0.0 when no water logged (empty DB)")
    void getTodayTotalMl_returnsZeroWhenNoLogs() {
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(any(), any())).thenReturn(0.0);

        double result = waterLogService.getTodayTotalMl(userId);

        assertEquals(0.0, result);
    }

    // -----------------------------------------------------------------------
    // logWater entry mapping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("logWater – log entry in response contains correct amountMl")
    void logWater_entryAmountIsCorrect() {
        WaterLogRequest request = new WaterLogRequest();
        request.setAmountMl(750.0);

        WaterLog savedLog = new WaterLog(userId, LocalDate.now(), 750.0, null);
        when(waterLogRepository.sumAmountMlByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(750.0);
        when(waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(savedLog));

        WaterTodayResponse response = waterLogService.logWater(request);

        assertEquals(1, response.getLogs().size());
        assertEquals(750.0, response.getLogs().get(0).getAmountMl());
    }
}
