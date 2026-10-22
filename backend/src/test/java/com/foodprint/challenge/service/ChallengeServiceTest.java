package com.foodprint.challenge.service;

import com.foodprint.challenge.dto.CheckinResponse;
import com.foodprint.challenge.dto.TodayStatusResponse;
import com.foodprint.challenge.dto.UserChallengeResponse;
import com.foodprint.challenge.entity.Challenge;
import com.foodprint.challenge.entity.ChallengeCheckin;
import com.foodprint.challenge.entity.UserChallenge;
import com.foodprint.challenge.exception.ChallengeException;
import com.foodprint.challenge.repository.ChallengeCheckinRepository;
import com.foodprint.challenge.repository.ChallengeRepository;
import com.foodprint.challenge.repository.UserChallengeRepository;
import com.foodprint.exercise.service.ExerciseLogService;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.food.entity.NutritionData;
import com.foodprint.food.repository.FoodLogRepository;
import com.foodprint.food.repository.NutritionDataRepository;
import com.foodprint.user.entity.User;
import com.foodprint.user.service.UserService;
import com.foodprint.water.service.WaterLogService;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChallengeService.
 *
 * All external dependencies are mocked via Mockito.
 * Authentication is simulated by setting a mock SecurityContext directly.
 *
 * IMPORTANT: WaterLogService and ExerciseLogService are mocked alongside the
 * food-log repos so that DAILY_WATER and CALORIE_BURN evaluation paths are tested
 * without needing a real Spring context.
 */
@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    // -----------------------------------------------------------------------
    // Mocks
    // -----------------------------------------------------------------------

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserChallengeRepository userChallengeRepository;
    @Mock private ChallengeCheckinRepository checkinRepository;
    @Mock private FoodLogRepository foodLogRepository;
    @Mock private NutritionDataRepository nutritionDataRepository;
    @Mock private UserService userService;
    @Mock private WaterLogService waterLogService;
    @Mock private ExerciseLogService exerciseLogService;

    @InjectMocks
    private ChallengeService challengeService;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private UUID userId;
    private Challenge proteinChallenge;
    private Challenge dailyWaterChallenge;
    private Challenge calorieBurnChallenge;
    private Challenge calorieControlChallenge;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        proteinChallenge        = new Challenge("Protein Champion",  "Desc", "PROTEIN_CHAMPION", 14, 50.0,   "💪");
        dailyWaterChallenge     = new Challenge("Hydration Hero",    "Desc", "DAILY_WATER",       7,  2000.0, "💧");
        calorieBurnChallenge    = new Challenge("Active Lifestyle",  "Desc", "CALORIE_BURN",       7,  200.0,  "🏃");
        calorieControlChallenge = new Challenge("Calorie Guardian",  "Desc", "CALORIE_CONTROL",   7,  0.0,    "🎯");
    }

    @AfterEach
    void tearDown() {
        // Always clear the security context so tests are fully isolated
        SecurityContextHolder.clearContext();
    }

    // -----------------------------------------------------------------------
    // Auth helper
    // -----------------------------------------------------------------------

    private void mockAuth() {
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn(userId.toString());
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // -----------------------------------------------------------------------
    // Join challenge
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Join Challenge – creates a new record when no prior record exists")
    void joinChallenge_success_newRecord() {
        mockAuth();
        UUID challengeId = UUID.randomUUID();
        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(proteinChallenge));
        when(userChallengeRepository.findByProfileIdAndChallengeId(userId, challengeId))
                .thenReturn(Optional.empty());
        when(userChallengeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserChallengeResponse response = challengeService.joinChallenge(challengeId);

        assertNotNull(response);
        assertEquals(userId, response.getProfileId());
        assertEquals(userId, response.getUserId());
        assertEquals(0, response.getCompletedDays());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertNotNull(response.getStartDate());
        assertNotNull(response.getEndDate());
        verify(userChallengeRepository).save(any(UserChallenge.class));
    }

    @Test
    @DisplayName("Join Challenge – resets an existing ABANDONED record instead of inserting a duplicate")
    void joinChallenge_resetsAbandonedRecord() {
        mockAuth();
        UUID challengeId = UUID.randomUUID();
        UserChallenge abandoned = new UserChallenge(
                userId, challengeId, LocalDate.now().minusDays(10), LocalDate.now().minusDays(4));
        abandoned.setStatus("ABANDONED");
        abandoned.setCompletedDays(3);
        abandoned.setCurrentStreak(2);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(proteinChallenge));
        when(userChallengeRepository.findByProfileIdAndChallengeId(userId, challengeId))
                .thenReturn(Optional.of(abandoned));
        when(userChallengeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserChallengeResponse response = challengeService.joinChallenge(challengeId);

        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(0, response.getCompletedDays());
        assertEquals(0, response.getCurrentStreak());
        // start/end dates must be reset to today
        assertEquals(LocalDate.now(), response.getStartDate());
        // Old checkin history must be deleted so the user can check in again on the same day
        verify(checkinRepository).deleteAllByUserChallengeId(abandoned.getId());
        // Only one save call — reuse of existing row, not a new insert
        verify(userChallengeRepository, times(1)).save(any(UserChallenge.class));
    }

    @Test
    @DisplayName("Join Challenge – resets an existing COMPLETED record on re-join")
    void joinChallenge_resetsCompletedRecord() {
        mockAuth();
        UUID challengeId = UUID.randomUUID();
        UserChallenge completed = new UserChallenge(
                userId, challengeId, LocalDate.now().minusDays(14), LocalDate.now().minusDays(1));
        completed.setStatus("COMPLETED");
        completed.setCompletedDays(14);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(proteinChallenge));
        when(userChallengeRepository.findByProfileIdAndChallengeId(userId, challengeId))
                .thenReturn(Optional.of(completed));
        when(userChallengeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserChallengeResponse response = challengeService.joinChallenge(challengeId);

        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(0, response.getCompletedDays());
        // Old checkin history must be deleted so the user can check in again on the same day
        verify(checkinRepository).deleteAllByUserChallengeId(completed.getId());
    }

    @Test
    @DisplayName("Join Challenge – throws ALREADY_IN_PROGRESS when existing record is IN_PROGRESS")
    void joinChallenge_failsWhenAlreadyInProgress() {
        mockAuth();
        UUID challengeId = UUID.randomUUID();
        UserChallenge active = new UserChallenge(
                userId, challengeId, LocalDate.now(), LocalDate.now().plusDays(13));

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(proteinChallenge));
        when(userChallengeRepository.findByProfileIdAndChallengeId(userId, challengeId))
                .thenReturn(Optional.of(active));

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.joinChallenge(challengeId));
        assertEquals("ALREADY_IN_PROGRESS", ex.getErrorCode());
    }

    @Test
    @DisplayName("Join Challenge – throws CHALLENGE_NOT_FOUND for unknown challenge ID")
    void joinChallenge_failsWhenChallengeNotFound() {
        mockAuth();
        UUID unknownId = UUID.randomUUID();
        when(challengeRepository.findById(unknownId)).thenReturn(Optional.empty());

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.joinChallenge(unknownId));
        assertEquals("CHALLENGE_NOT_FOUND", ex.getErrorCode());
    }

    // -----------------------------------------------------------------------
    // Check-in: PROTEIN_CHAMPION (food-log integration)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin PROTEIN_CHAMPION – passes when daily protein ≥ target")
    void checkin_proteinChampion_passes() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(13));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(proteinChallenge));

        FoodLog log = new FoodLog();
        log.setId(UUID.randomUUID());
        when(foodLogRepository.findByUserIdAndLogDate(eq(userId), any())).thenReturn(List.of(log));

        NutritionData nutrition = new NutritionData();
        nutrition.setProteinG(60.0); // ≥ 50g target
        when(nutritionDataRepository.findByFoodLogId(log.getId())).thenReturn(List.of(nutrition));

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
        assertEquals(60.0, response.getActualValue());
        assertEquals(1, response.getCompletedDays());
        assertTrue(response.getNote().contains("✅"));
    }

    @Test
    @DisplayName("Checkin PROTEIN_CHAMPION – fails when daily protein < target")
    void checkin_proteinChampion_fails() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(13));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(proteinChallenge));

        FoodLog log = new FoodLog();
        log.setId(UUID.randomUUID());
        when(foodLogRepository.findByUserIdAndLogDate(eq(userId), any())).thenReturn(List.of(log));

        NutritionData nutrition = new NutritionData();
        nutrition.setProteinG(20.0); // < 50g target
        when(nutritionDataRepository.findByFoodLogId(log.getId())).thenReturn(List.of(nutrition));

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertFalse(response.isPassed());
        assertEquals(20.0, response.getActualValue());
        assertEquals(0, response.getCompletedDays());
        assertTrue(response.getNote().contains("❌"));
    }

    // -----------------------------------------------------------------------
    // Check-in: CALORIE_CONTROL (reads user's personal target from UserService)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin CALORIE_CONTROL – passes when calories within 80-110% of user's target")
    void checkin_calorieControl_passes() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(calorieControlChallenge));

        // User has 2000 kcal/day target — 80-110% window is 1600–2200
        User user = new User();
        user.setDailyCalorieTarget(2000.0);
        when(userService.getUserById(userId.toString())).thenReturn(user);

        FoodLog log = new FoodLog();
        log.setId(UUID.randomUUID());
        when(foodLogRepository.findByUserIdAndLogDate(eq(userId), any())).thenReturn(List.of(log));

        NutritionData nutrition = new NutritionData();
        nutrition.setCalories(1800.0); // within 1600–2200
        when(nutritionDataRepository.findByFoodLogId(log.getId())).thenReturn(List.of(nutrition));

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
        assertEquals(1800.0, response.getActualValue());
        assertTrue(response.getNote().contains("✅"));
    }

    @Test
    @DisplayName("Checkin CALORIE_CONTROL – falls back to 2000 kcal when user has no target set")
    void checkin_calorieControl_fallbackTargetWhenNoUserTarget() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(calorieControlChallenge));

        // User found but dailyCalorieTarget is null → service must fall back to 2000
        User user = new User();
        user.setDailyCalorieTarget(null);
        when(userService.getUserById(userId.toString())).thenReturn(user);

        FoodLog log = new FoodLog();
        log.setId(UUID.randomUUID());
        when(foodLogRepository.findByUserIdAndLogDate(eq(userId), any())).thenReturn(List.of(log));

        NutritionData nutrition = new NutritionData();
        nutrition.setCalories(1900.0); // within 80-110% of fallback 2000 (1600–2200)
        when(nutritionDataRepository.findByFoodLogId(log.getId())).thenReturn(List.of(nutrition));

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
    }

    // -----------------------------------------------------------------------
    // Check-in: DAILY_WATER (water-log integration)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin DAILY_WATER – passes when total water ≥ 2000 ml target")
    void checkin_dailyWater_passes() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));

        // 2500 ml logged today — above the 2000 ml target
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(2500.0);
        // Food-log repos are not called for DAILY_WATER; no need to mock them.

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
        assertEquals(2500.0, response.getActualValue());
        assertTrue(response.getNote().contains("2500"));
        assertTrue(response.getNote().contains("✅"));
        // WaterLogService must be called with the correct userId, NOT with any proxy or wrong UUID
        verify(waterLogService).getTodayTotalMl(userId);
    }

    @Test
    @DisplayName("Checkin DAILY_WATER – fails when water logged is below 2000 ml target")
    void checkin_dailyWater_fails() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));

        // Only 800 ml logged — below 2000 ml target
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(800.0);

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertFalse(response.isPassed());
        assertEquals(800.0, response.getActualValue());
        assertTrue(response.getNote().contains("❌"));
        assertEquals(0, response.getCompletedDays());
    }

    @Test
    @DisplayName("Checkin DAILY_WATER – exactly at goal (2000 ml) counts as passed")
    void checkin_dailyWater_exactlyAtGoal() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(2000.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed(), "Exactly 2000 ml should pass (>=)");
    }

    // -----------------------------------------------------------------------
    // Check-in: CALORIE_BURN (exercise-log integration)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin CALORIE_BURN – passes when calories burned ≥ 200 kcal target")
    void checkin_calorieBurn_passes() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(calorieBurnChallenge));

        // 350 kcal burned — above 200 target
        when(exerciseLogService.getTodayCaloriesBurned(userId)).thenReturn(350.0);

        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
        assertEquals(350.0, response.getActualValue());
        assertTrue(response.getNote().contains("350"));
        assertTrue(response.getNote().contains("✅"));
        // ExerciseLogService must be called with the correct userId
        verify(exerciseLogService).getTodayCaloriesBurned(userId);
    }

    @Test
    @DisplayName("Checkin CALORIE_BURN – fails when no exercise logged (0 kcal)")
    void checkin_calorieBurn_failsWhenNoExercise() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(calorieBurnChallenge));
        when(exerciseLogService.getTodayCaloriesBurned(userId)).thenReturn(0.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertFalse(response.isPassed());
        assertEquals(0.0, response.getActualValue());
        assertTrue(response.getNote().contains("❌"));
    }

    @Test
    @DisplayName("Checkin CALORIE_BURN – exactly at goal (200 kcal) counts as passed")
    void checkin_calorieBurn_exactlyAtGoal() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(calorieBurnChallenge));
        when(exerciseLogService.getTodayCaloriesBurned(userId)).thenReturn(200.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed(), "Exactly 200 kcal should pass (>=)");
    }

    // -----------------------------------------------------------------------
    // Check-in: guard conditions
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin – throws ALREADY_CHECKED_IN when second check-in on same day")
    void checkin_throwsWhenAlreadyCheckedInToday() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(ucId, LocalDate.now()))
                .thenReturn(true);

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.checkin(ucId));
        assertEquals("ALREADY_CHECKED_IN", ex.getErrorCode());
    }

    @Test
    @DisplayName("Checkin – throws CHALLENGE_NOT_ACTIVE when challenge is COMPLETED")
    void checkin_throwsWhenChallengeCompleted() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now().minusDays(7), LocalDate.now().minusDays(1));
        uc.setStatus("COMPLETED");

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.checkin(ucId));
        assertEquals("CHALLENGE_NOT_ACTIVE", ex.getErrorCode());
    }

    // -----------------------------------------------------------------------
    // Check-in: auto-completion when all days reached
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin – challenge auto-completes when completedDays reaches durationDays")
    void checkin_challengeAutoCompletes() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        // A 7-day challenge with 6 days already done — one more pass should complete it
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now().minusDays(6), LocalDate.now());
        uc.setCompletedDays(6);

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(
                new Challenge("Hydration Hero", "Desc", "DAILY_WATER", 7, 2000.0, "💧")));
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(2100.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(any())).thenReturn(List.of());

        CheckinResponse response = challengeService.checkin(ucId);

        assertTrue(response.isPassed());
        assertTrue(response.isChallengeCompleted());
        assertEquals(7, response.getCompletedDays());
        assertTrue(response.getMessage().contains("Congratulations") || response.getMessage().contains("🎉"));
    }

    // -----------------------------------------------------------------------
    // Streak calculation (via checkin)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Streak – counts consecutive passed days and stops at first failure")
    void calculateStreak_multipleDays() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        // Three prior check-ins: today(pass), yesterday(pass), 2-days-ago(fail)
        // Using DAILY_WATER so today's evaluation also passes (water=2500 >= 2000 target),
        // keeping the evaluation result consistent with the mocked DB streak state.
        ChallengeCheckin c1 = new ChallengeCheckin(ucId, userId, today,              true,  2500.0, "Note");
        ChallengeCheckin c2 = new ChallengeCheckin(ucId, userId, today.minusDays(1), true,  2100.0, "Note");
        ChallengeCheckin c3 = new ChallengeCheckin(ucId, userId, today.minusDays(2), false, 900.0,  "Note");

        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(ucId))
                .thenReturn(List.of(c1, c2, c3));

        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), today.minusDays(5), today.plusDays(5));
        when(userChallengeRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));
        // Today: 2500 ml → passes DAILY_WATER (≥ 2000 ml target)
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(2500.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);

        CheckinResponse response = challengeService.checkin(ucId);

        // Today passes; c1 and c2 are consecutive passes; c3 breaks the streak → streak = 2
        assertTrue(response.isPassed(), "Today should pass with 2500 ml water");
        assertEquals(2, response.getCurrentStreak());
    }

    @Test
    @DisplayName("Streak – resets to zero when today's check-in fails and no prior passed day exists")
    void calculateStreak_zeroWhenTodayFails() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        // No prior check-ins at all
        when(checkinRepository.findByUserChallengeIdOrderByCheckinDateDesc(ucId))
                .thenReturn(List.of());

        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), today.minusDays(3), today.plusDays(3));
        when(userChallengeRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(uc));
        // Use DAILY_WATER with 0 ml logged → evaluation fails (0 < 2000 target)
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(0.0);
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);

        CheckinResponse response = challengeService.checkin(ucId);

        assertFalse(response.isPassed());
        assertEquals(0, response.getCurrentStreak());
    }

    // -----------------------------------------------------------------------
    // Today status (read-only preview)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getTodayStatus – returns preview metrics when user has NOT checked in yet")
    void getTodayStatus_notCheckedIn_returnsPreview() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));
        uc.setCompletedDays(3);

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));
        // Not checked in today
        when(checkinRepository.findByUserChallengeIdAndCheckinDate(ucId, LocalDate.now()))
                .thenReturn(Optional.empty());
        // Current water = 1200 ml (below 2000 target)
        when(waterLogService.getTodayTotalMl(userId)).thenReturn(1200.0);

        TodayStatusResponse status = challengeService.getTodayStatus(ucId);

        assertFalse(status.isCheckedIn());
        assertNull(status.getPassed(), "passed must be null when not yet checked in");
        assertEquals(1200.0, status.getCurrentValue());
        assertEquals(2000.0, status.getTargetValue());
        assertEquals(6, status.getDaysRemaining());   // endDate is now+6 → 6 days including today
        // progress: completedDays=3, durationDays=7 → 3*100/7 = 42%
        assertEquals(42, status.getProgressPercent());
        assertNotNull(status.getSummary());
    }

    @Test
    @DisplayName("getTodayStatus – returns actual result when user HAS already checked in today")
    void getTodayStatus_alreadyCheckedIn_returnsActualResult() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now().minusDays(2), LocalDate.now().plusDays(4));
        uc.setCompletedDays(2);
        uc.setCurrentStreak(2);

        ChallengeCheckin todayCheckin = new ChallengeCheckin(
                ucId, userId, LocalDate.now(), true, 2300.0, "Today's water: 2300 ml ✅");

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(dailyWaterChallenge));
        when(checkinRepository.findByUserChallengeIdAndCheckinDate(ucId, LocalDate.now()))
                .thenReturn(Optional.of(todayCheckin));
        // evaluateToday is now always called (even when already checked in) to populate nutritionSnapshot
        when(waterLogService.getTodayTotalMl(any())).thenReturn(2300.0);
        when(exerciseLogService.getTodayCaloriesBurned(any())).thenReturn(0.0);
        when(foodLogRepository.findByUserIdAndLogDate(any(), any())).thenReturn(List.of());

        TodayStatusResponse status = challengeService.getTodayStatus(ucId);

        assertTrue(status.isCheckedIn());
        assertTrue(status.getPassed());
        assertEquals(2300.0, status.getCurrentValue());
        assertEquals(2000.0, status.getTargetValue());
        assertEquals("Today's water: 2300 ml \u2705", status.getSummary());
        assertEquals(2, status.getCurrentStreak());
        assertNotNull(status.getNutritionSnapshot());
        assertEquals(2300.0, status.getNutritionSnapshot().getWaterMl());
    }

    // -----------------------------------------------------------------------
    // Active challenges enrichment (todayCheckedIn / todayPassed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getMyActiveChallenges – enriches each challenge with today's check-in status")
    void getMyActiveChallenges_enrichedWithTodayStatus() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByUserIdAndStatus(userId, "IN_PROGRESS"))
                .thenReturn(List.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(proteinChallenge));

        // Checked in today, and passed
        ChallengeCheckin todayCheckin = new ChallengeCheckin(
                ucId, userId, LocalDate.now(), true, 55.0, "Note");
        when(checkinRepository.findByUserChallengeIdAndCheckinDate(uc.getId(), LocalDate.now()))
                .thenReturn(Optional.of(todayCheckin));

        List<UserChallengeResponse> active = challengeService.getMyActiveChallenges();

        assertEquals(1, active.size());
        UserChallengeResponse card = active.get(0);
        assertTrue(card.isTodayCheckedIn());
        assertTrue(card.getTodayPassed());
    }

    @Test
    @DisplayName("getMyActiveChallenges – todayCheckedIn is false when no check-in today")
    void getMyActiveChallenges_todayNotCheckedIn() {
        mockAuth();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByUserIdAndStatus(userId, "IN_PROGRESS"))
                .thenReturn(List.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(proteinChallenge));
        when(checkinRepository.findByUserChallengeIdAndCheckinDate(uc.getId(), LocalDate.now()))
                .thenReturn(Optional.empty());

        List<UserChallengeResponse> active = challengeService.getMyActiveChallenges();

        assertFalse(active.get(0).isTodayCheckedIn());
        assertNull(active.get(0).getTodayPassed());
    }

    // -----------------------------------------------------------------------
    // Abandon challenge
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Abandon – successfully sets status to ABANDONED")
    void abandonChallenge_success() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));

        challengeService.abandonChallenge(ucId);

        verify(userChallengeRepository).save(argThat(saved -> "ABANDONED".equals(saved.getStatus())));
    }

    @Test
    @DisplayName("Abandon – throws CHALLENGE_NOT_ACTIVE when challenge is already COMPLETED")
    void abandonChallenge_failsWhenAlreadyCompleted() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now().minusDays(7), LocalDate.now().minusDays(1));
        uc.setStatus("COMPLETED");

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.abandonChallenge(ucId));
        assertEquals("CHALLENGE_NOT_ACTIVE", ex.getErrorCode());
    }

    // -----------------------------------------------------------------------
    // Cross-user access prevention
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin – throws NOT_FOUND when the challenge belongs to a different user")
    void checkin_throwsWhenChallengeNotOwnedByUser() {
        mockAuth();
        UUID ucId = UUID.randomUUID();

        // findByIdAndUserId returns empty → cross-user access denied
        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.empty());

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.checkin(ucId));
        assertEquals("NOT_FOUND", ex.getErrorCode());
    }

    // -----------------------------------------------------------------------
    // Bug-fix: timestamp set in constructor (not relying solely on @PrePersist)
    // Spring Data JPA calls merge() for pre-assigned UUIDs (isNew() == false),
    // and @PrePersist is not guaranteed to fire during merge() in all Hibernate
    // configurations. Setting timestamps in the constructor prevents null
    // NOT-NULL constraint violations that caused HTTP 500 errors.
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("UserChallenge – joinedAt is set immediately in constructor, not via @PrePersist only")
    void userChallenge_joinedAtSetInConstructor() {
        UserChallenge uc = new UserChallenge(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(6));

        assertNotNull(uc.getJoinedAt(),
                "joinedAt must be non-null immediately after construction; " +
                "relying solely on @PrePersist is unsafe when merge() is used");
    }

    @Test
    @DisplayName("ChallengeCheckin – createdAt is set immediately in constructor, not via @PrePersist only")
    void challengeCheckin_createdAtSetInConstructor() {
        ChallengeCheckin checkin = new ChallengeCheckin(
                UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), true, 2000.0, "note");

        assertNotNull(checkin.getCreatedAt(),
                "createdAt must be non-null immediately after construction; " +
                "relying solely on @PrePersist is unsafe when merge() is used");
    }

    @Test
    @DisplayName("Challenge – createdAt is set immediately in constructor, not via @PrePersist only")
    void challenge_createdAtSetInConstructor() {
        Challenge c = new Challenge("Title", "Desc", "LOG_STREAK", 7, 1.0, "📝");

        assertNotNull(c.getCreatedAt(),
                "createdAt must be non-null immediately after construction; " +
                "relying solely on @PrePersist is unsafe when merge() is used");
    }

    // -----------------------------------------------------------------------
    // Bug-fix: DB integrity violation converted to 400 (not 500)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Checkin – DataIntegrityViolationException on save is converted to ALREADY_CHECKED_IN (400, not 500)")
    void checkin_dbConstraintViolation_throwsChallengeException() {
        mockAuth();
        UUID ucId = UUID.randomUUID();
        UserChallenge uc = new UserChallenge(
                userId, UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusDays(6));

        when(userChallengeRepository.findByIdAndUserId(ucId, userId)).thenReturn(Optional.of(uc));
        when(challengeRepository.findById(any())).thenReturn(Optional.of(proteinChallenge));
        when(checkinRepository.existsByUserChallengeIdAndCheckinDate(any(), any())).thenReturn(false);
        when(foodLogRepository.findByUserIdAndLogDate(any(), any())).thenReturn(List.of());
        // DB fires unique constraint even though the service pre-check passed (race condition)
        when(checkinRepository.save(any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("unique_checkin constraint violation"));

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.checkin(ucId));
        assertEquals("ALREADY_CHECKED_IN", ex.getErrorCode());
    }

    // -----------------------------------------------------------------------
    // Create challenge
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Create Challenge – success creates and returns new challenge template")
    void createChallenge_success() {
        com.foodprint.challenge.dto.ChallengeCreateRequest request =
                new com.foodprint.challenge.dto.ChallengeCreateRequest();
        request.setTitle("My Custom Challenge");
        request.setDescription("Do something awesome every day");
        request.setType("MY_CUSTOM");
        request.setDurationDays(10);
        request.setTargetValue(500.0);
        request.setBadgeIcon("⭐");

        when(challengeRepository.existsByType("MY_CUSTOM")).thenReturn(false);
        when(challengeRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        com.foodprint.challenge.dto.ChallengeResponse response =
                challengeService.createChallenge(request);

        assertNotNull(response);
        assertEquals("My Custom Challenge", response.getTitle());
        assertEquals("MY_CUSTOM", response.getType());
        assertEquals(10, response.getDurationDays());
        assertEquals(500.0, response.getTargetValue());
        assertEquals("⭐", response.getBadgeIcon());
        verify(challengeRepository).save(any(Challenge.class));
    }

    @Test
    @DisplayName("Create Challenge – throws when type already exists")
    void createChallenge_throwsOnDuplicateType() {
        com.foodprint.challenge.dto.ChallengeCreateRequest request =
                new com.foodprint.challenge.dto.ChallengeCreateRequest();
        request.setTitle("Duplicate");
        request.setType("PROTEIN_CHAMPION");
        request.setDurationDays(7);

        when(challengeRepository.existsByType("PROTEIN_CHAMPION")).thenReturn(true);

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.createChallenge(request));
        assertEquals("DUPLICATE_TYPE", ex.getErrorCode());
        verify(challengeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Challenge – throws when title is blank")
    void createChallenge_throwsOnBlankTitle() {
        com.foodprint.challenge.dto.ChallengeCreateRequest request =
                new com.foodprint.challenge.dto.ChallengeCreateRequest();
        request.setTitle("  ");
        request.setType("SOME_TYPE");
        request.setDurationDays(7);

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.createChallenge(request));
        assertEquals("INVALID_REQUEST", ex.getErrorCode());
    }

    @Test
    @DisplayName("Create Challenge – throws when durationDays is zero or negative")
    void createChallenge_throwsOnInvalidDuration() {
        com.foodprint.challenge.dto.ChallengeCreateRequest request =
                new com.foodprint.challenge.dto.ChallengeCreateRequest();
        request.setTitle("Valid Title");
        request.setType("SOME_TYPE");
        request.setDurationDays(0);

        ChallengeException ex = assertThrows(ChallengeException.class,
                () -> challengeService.createChallenge(request));
        assertEquals("INVALID_REQUEST", ex.getErrorCode());
    }
}
