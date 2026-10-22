package com.foodprint.exercise.service;

import com.foodprint.exercise.dto.ExerciseLogRequest;
import com.foodprint.exercise.dto.ExerciseTodayResponse;
import com.foodprint.exercise.entity.ExerciseLog;
import com.foodprint.exercise.repository.ExerciseLogRepository;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExerciseLogService.
 *
 * Covers: logging exercise sessions, aggregating today's totals, input validation,
 * and the internal query method used by ChallengeService for CALORIE_BURN evaluation.
 */
@ExtendWith(MockitoExtension.class)
class ExerciseLogServiceTest {

    @Mock
    private ExerciseLogRepository exerciseLogRepository;

    @InjectMocks
    private ExerciseLogService exerciseLogService;

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
    // logExercise – happy path
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("logExercise – saves the session and returns updated today summary")
    void logExercise_success() {
        ExerciseLogRequest request = buildRequest("Running", 300.0, 30);

        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(300.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(30L);

        ExerciseLog savedLog = new ExerciseLog(userId, LocalDate.now(), "Running", 300.0, 30, null);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(savedLog));

        ExerciseTodayResponse response = exerciseLogService.logExercise(request);

        assertNotNull(response);
        assertEquals(300.0, response.getTotalCaloriesBurned());
        assertEquals(30, response.getTotalDurationMin());
        assertEquals(1, response.getExercises().size());

        verify(exerciseLogRepository, times(1)).save(any(ExerciseLog.class));
    }

    @Test
    @DisplayName("logExercise – note field is optional; succeeds without it")
    void logExercise_nullNoteSucceeds() {
        ExerciseLogRequest request = buildRequest("Cycling", 250.0, 45);
        request.setNote(null); // explicitly null

        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(250.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(45L);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> exerciseLogService.logExercise(request));
    }

    @Test
    @DisplayName("logExercise – durationMin is optional; succeeds when null")
    void logExercise_nullDurationSucceeds() {
        ExerciseLogRequest request = buildRequest("Yoga", 100.0, null);

        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(100.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(0L);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> exerciseLogService.logExercise(request));
    }

    // -----------------------------------------------------------------------
    // logExercise – validation guards
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("logExercise – throws when activityType is blank")
    void logExercise_throwsOnBlankActivityType() {
        ExerciseLogRequest request = buildRequest("   ", 300.0, 30);

        assertThrows(IllegalArgumentException.class, () -> exerciseLogService.logExercise(request));
        verify(exerciseLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("logExercise – throws when activityType is null")
    void logExercise_throwsOnNullActivityType() {
        ExerciseLogRequest request = buildRequest(null, 300.0, 30);

        assertThrows(IllegalArgumentException.class, () -> exerciseLogService.logExercise(request));
        verify(exerciseLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("logExercise – throws when caloriesBurned is zero")
    void logExercise_throwsOnZeroCalories() {
        ExerciseLogRequest request = buildRequest("Running", 0.0, 30);

        assertThrows(IllegalArgumentException.class, () -> exerciseLogService.logExercise(request));
        verify(exerciseLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("logExercise – throws when caloriesBurned is negative")
    void logExercise_throwsOnNegativeCalories() {
        ExerciseLogRequest request = buildRequest("Running", -100.0, 30);

        assertThrows(IllegalArgumentException.class, () -> exerciseLogService.logExercise(request));
        verify(exerciseLogRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getTodayExercise
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTodayExercise – returns zero summary when no sessions logged today")
    void getTodayExercise_noSessions() {
        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(0.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(0L);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of());

        ExerciseTodayResponse response = exerciseLogService.getTodayExercise();

        assertNotNull(response);
        assertEquals(0.0, response.getTotalCaloriesBurned());
        assertEquals(0, response.getTotalDurationMin());
        assertTrue(response.getExercises().isEmpty());
    }

    @Test
    @DisplayName("getTodayExercise – aggregates multiple sessions correctly")
    void getTodayExercise_multipleSessions() {
        // Two sessions: 300 kcal / 30 min + 200 kcal / 20 min = 500 kcal / 50 min
        ExerciseLog log1 = new ExerciseLog(userId, LocalDate.now(), "Running", 300.0, 30, null);
        ExerciseLog log2 = new ExerciseLog(userId, LocalDate.now(), "Cycling", 200.0, 20, null);

        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(500.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(50L);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(log1, log2));

        ExerciseTodayResponse response = exerciseLogService.getTodayExercise();

        assertEquals(500.0, response.getTotalCaloriesBurned());
        assertEquals(50, response.getTotalDurationMin());
        assertEquals(2, response.getExercises().size());
    }

    @Test
    @DisplayName("getTodayExercise – entry fields are mapped from entity correctly")
    void getTodayExercise_entryFieldsAreMapped() {
        ExerciseLog log = new ExerciseLog(userId, LocalDate.now(), "Swimming", 400.0, 60, "evening swim");

        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(400.0);
        when(exerciseLogRepository.sumDurationMinByUserIdAndLogDate(eq(userId), any(LocalDate.class)))
                .thenReturn(60L);
        when(exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(eq(userId), any(LocalDate.class)))
                .thenReturn(List.of(log));

        ExerciseTodayResponse response = exerciseLogService.getTodayExercise();

        ExerciseTodayResponse.ExerciseEntry entry = response.getExercises().get(0);
        assertEquals("Swimming", entry.getActivityType());
        assertEquals(400.0, entry.getCaloriesBurned());
        assertEquals(60, entry.getDurationMin());
        assertEquals("evening swim", entry.getNote());
    }

    // -----------------------------------------------------------------------
    // getTodayCaloriesBurned (used by ChallengeService)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTodayCaloriesBurned – queries with the supplied userId, NOT the auth context")
    void getTodayCaloriesBurned_usesSuppliedUserId() {
        UUID differentUserId = UUID.randomUUID();
        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(
                eq(differentUserId), any(LocalDate.class))).thenReturn(250.0);

        double result = exerciseLogService.getTodayCaloriesBurned(differentUserId);

        assertEquals(250.0, result);
        verify(exerciseLogRepository)
                .sumCaloriesBurnedByUserIdAndLogDate(eq(differentUserId), any(LocalDate.class));
        // Must NOT fall back to the auth context userId
        verify(exerciseLogRepository, never())
                .sumCaloriesBurnedByUserIdAndLogDate(eq(userId), any(LocalDate.class));
    }

    @Test
    @DisplayName("getTodayCaloriesBurned – returns 0.0 when no exercise logged")
    void getTodayCaloriesBurned_returnsZero() {
        when(exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(any(), any())).thenReturn(0.0);

        assertEquals(0.0, exerciseLogService.getTodayCaloriesBurned(userId));
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private ExerciseLogRequest buildRequest(String activityType, double caloriesBurned,
                                             Integer durationMin) {
        ExerciseLogRequest req = new ExerciseLogRequest();
        req.setActivityType(activityType);
        req.setCaloriesBurned(caloriesBurned);
        req.setDurationMin(durationMin);
        return req;
    }
}
