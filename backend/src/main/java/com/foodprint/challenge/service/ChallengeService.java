package com.foodprint.challenge.service;

import com.foodprint.challenge.dto.ChallengeCreateRequest;
import com.foodprint.challenge.dto.ChallengeResponse;
import com.foodprint.challenge.dto.CheckinResponse;
import com.foodprint.challenge.dto.NutritionSnapshot;
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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private UserChallengeRepository userChallengeRepository;
    @Autowired private ChallengeCheckinRepository checkinRepository;
    @Autowired private FoodLogRepository foodLogRepository;
    @Autowired private NutritionDataRepository nutritionDataRepository;
    @Autowired private UserService userService;
    @Autowired private WaterLogService waterLogService;
    @Autowired private ExerciseLogService exerciseLogService;

    // =========================================================================
    // Authentication Helper
    // =========================================================================

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ChallengeException("User not authenticated", "UNAUTHORIZED");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new ChallengeException("Invalid user ID format in token", "INVALID_IDENTITY");
        }
    }

    /**
     * Looks up a UserChallenge by its own ID and the current user's identity.
     * Tries profile_id first (Supabase JWT sub == profile_id), falls back to user_id.
     * Throws ChallengeException if not found or not owned by the current user.
     */
    private UserChallenge findUserChallengeByIdForCurrentUser(UUID userChallengeId) {
        UUID userId = getCurrentUserId();
        return userChallengeRepository.findByIdAndProfileId(userChallengeId, userId)
                .or(() -> userChallengeRepository.findByIdAndUserId(userChallengeId, userId))
                .orElseThrow(() -> new ChallengeException(
                        "Challenge record not found or access denied.", "NOT_FOUND"));
    }
    // =========================================================================

    @PostConstruct
    public void seedChallenges() {
        seedIfAbsent("CALORIE_CONTROL", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-CALORIE_CONTROL".getBytes()),
                "Calorie Guardian",
                "Keep your daily calorie intake within 80%-110% of your personal target " +
                "for 7 consecutive days. Master precise calorie control — the first step to healthy eating!",
                "CALORIE_CONTROL", 7, 0.0, "🎯"));
        seedIfAbsent("PROTEIN_CHAMPION", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-PROTEIN_CHAMPION".getBytes()),
                "Protein Champion",
                "Hit a daily protein intake of at least 50g for 14 consecutive days. " +
                "Fuel muscle recovery and growth — get stronger every day!",
                "PROTEIN_CHAMPION", 14, 50.0, "💪"));
        seedIfAbsent("LOG_STREAK", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-LOG_STREAK".getBytes()),
                "Food Diary Hero",
                "Log your meals at least once every day for 7 consecutive days. " +
                "Tracking is the start of change — consistency is the victory!",
                "LOG_STREAK", 7, 1.0, "📝"));
        seedIfAbsent("LOW_CARB", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-LOW_CARB".getBytes()),
                "Low-Carb Warrior",
                "Keep daily carbohydrate intake at or below 150g for 14 consecutive days. " +
                "Experience stable energy and mental clarity on a low-carb diet!",
                "LOW_CARB", 14, 150.0, "⚡"));
        seedIfAbsent("LIGHT_EATER", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-LIGHT_EATER".getBytes()),
                "Light Eater",
                "Keep daily calories at or below 1500 kcal (with at least one food log) " +
                "for 7 consecutive days. Eat clean, feel light!",
                "LIGHT_EATER", 7, 1500.0, "🥗"));
        seedIfAbsent("DAILY_WATER", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-DAILY_WATER".getBytes()),
                "Hydration Hero",
                "Drink at least 2000 ml (about 8 cups) of water every day for 7 consecutive days. " +
                "Proper hydration boosts energy, focus, and metabolism!",
                "DAILY_WATER", 7, 2000.0, "💧"));
        seedIfAbsent("CALORIE_BURN", new Challenge(
                UUID.nameUUIDFromBytes("Challenge-CALORIE_BURN".getBytes()),
                "Active Lifestyle",
                "Burn at least 200 calories through exercise every day for 7 consecutive days. " +
                "Move your body, boost your metabolism, and feel great!",
                "CALORIE_BURN", 7, 200.0, "🏃"));
    }

    private void seedIfAbsent(String type, Challenge challenge) {
        if (!challengeRepository.existsByType(type)) {
            challengeRepository.save(challenge);
        }
    }

    // =========================================================================
    // Public read
    // =========================================================================

    /** Returns all available challenge templates. */
    public List<ChallengeResponse> getAllChallenges() {
        return challengeRepository.findAll().stream()
                .map(this::toChallengeResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new custom challenge template.
     * The type field uniquely identifies each challenge — if a challenge with
     * the same type already exists, a ChallengeException is thrown.
     */
    @Transactional
    public ChallengeResponse createChallenge(ChallengeCreateRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ChallengeException("Challenge title is required.", "INVALID_REQUEST");
        }
        if (request.getType() == null || request.getType().isBlank()) {
            throw new ChallengeException("Challenge type is required.", "INVALID_REQUEST");
        }
        if (request.getDurationDays() <= 0) {
            throw new ChallengeException("Duration must be at least 1 day.", "INVALID_REQUEST");
        }
        if (challengeRepository.existsByType(request.getType())) {
            throw new ChallengeException(
                    "A challenge with type '" + request.getType() + "' already exists.",
                    "DUPLICATE_TYPE");
        }
        Challenge challenge = new Challenge(
                request.getTitle(),
                request.getDescription() != null ? request.getDescription() : "",
                request.getType(),
                request.getDurationDays(),
                request.getTargetValue(),
                request.getBadgeIcon() != null ? request.getBadgeIcon() : "🏆"
        );
        challenge = challengeRepository.save(challenge);
        return toChallengeResponse(challenge);
    }

    // =========================================================================
    // User challenge operations
    // =========================================================================

    /**
     * Join a challenge — upsert style.
     *
     * The database enforces a unique constraint on (profile_id, challenge_id), so only one
     * record per user per challenge template may exist at any time.
     *
     * Logic:
     *  - If no record exists: create a fresh one.
     *  - If a record exists with status IN_PROGRESS: reject (user must complete or abandon first).
     *  - If a record exists with status ABANDONED or COMPLETED: reset it so the user starts again.
     *    History (check-ins) is preserved in the challenge_checkins table but the counters reset.
     */
    @Transactional
    public UserChallengeResponse joinChallenge(UUID challengeId) {
        UUID userId = getCurrentUserId();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ChallengeException(
                        "Challenge not found: " + challengeId, "CHALLENGE_NOT_FOUND"));

        LocalDate startDate = LocalDate.now();
        LocalDate endDate   = startDate.plusDays(challenge.getDurationDays() - 1);

        Optional<UserChallenge> existing =
                userChallengeRepository.findByProfileIdAndChallengeId(userId, challengeId);

        UserChallenge uc;
        if (existing.isPresent()) {
            uc = existing.get();
            if ("IN_PROGRESS".equals(uc.getStatus())) {
                throw new ChallengeException(
                        "You are already doing this challenge. Complete or abandon it before joining again.",
                        "ALREADY_IN_PROGRESS");
            }
            // Reset the existing record for a fresh run.
            // Old check-in history is DELETED so the user can check in again on the same day.
            checkinRepository.deleteAllByUserChallengeId(uc.getId());
            uc.setStatus("IN_PROGRESS");
            uc.setStartDate(startDate);
            uc.setEndDate(endDate);
            uc.setCompletedDays(0);
            uc.setProgress(0);
            uc.setCurrentStreak(0);
            uc.setCompletedAt(null);
            uc.setJoinedAt(LocalDateTime.now());
        } else {
            uc = new UserChallenge(userId, challengeId, startDate, endDate);
        }

        uc = userChallengeRepository.save(uc);
        return toUserChallengeResponse(uc, challenge, Collections.emptyList());
    }

    /**
     * Returns all of the user's challenge records (all statuses).
     * Check-in history is omitted to keep the payload small.
     */
    public List<UserChallengeResponse> getMyChallenges() {
        UUID userId = getCurrentUserId();
        return userChallengeRepository.findByUserId(userId).stream()
                .map(uc -> {
                    Challenge c = challengeRepository.findById(uc.getChallengeId()).orElse(null);
                    return toUserChallengeResponse(uc, c, Collections.emptyList());
                })
                .collect(Collectors.toList());
    }

    /** Returns only in-progress challenge records with today's check-in status. */
    public List<UserChallengeResponse> getMyActiveChallenges() {
        UUID userId = getCurrentUserId();
        LocalDate today = LocalDate.now();
        return userChallengeRepository.findByUserIdAndStatus(userId, "IN_PROGRESS").stream()
                .map(uc -> {
                    Challenge c = challengeRepository.findById(uc.getChallengeId()).orElse(null);
                    UserChallengeResponse resp = toUserChallengeResponse(uc, c, Collections.emptyList());
                    // Populate today's check-in status for home-screen cards
                    Optional<ChallengeCheckin> todayCheckin =
                            checkinRepository.findByUserChallengeIdAndCheckinDate(uc.getId(), today);
                    resp.setTodayCheckedIn(todayCheckin.isPresent());
                    resp.setTodayPassed(todayCheckin.map(ChallengeCheckin::isPassed).orElse(null));
                    return resp;
                })
                .collect(Collectors.toList());
    }

    /** Returns full detail for one challenge record, including the complete check-in history. */
    public UserChallengeResponse getUserChallengeDetail(UUID userChallengeId) {
        UserChallenge uc = findUserChallengeByIdForCurrentUser(userChallengeId);

        Challenge challenge = challengeRepository.findById(uc.getChallengeId())
                .orElseThrow(() -> new ChallengeException(
                        "Challenge data error.", "DATA_ERROR"));

        List<CheckinResponse> checkins = checkinRepository
                .findByUserChallengeIdOrderByCheckinDateAsc(uc.getId())
                .stream()
                .map(c -> new CheckinResponse(
                        c.getCheckinDate(), c.isPassed(), c.getActualValue(),
                        c.getNote(), 0, 0, false, null))
                .collect(Collectors.toList());

        return toUserChallengeResponse(uc, challenge, checkins);
    }

    /**
     * Daily check-in.
     * Reads today's food logs from the database, calculates the relevant nutrition
     * metric, and evaluates it against the challenge criteria.
     *
     * Rules:
     *  - Challenge must be IN_PROGRESS
     *  - Today must fall within startDate..endDate
     *  - Only one check-in per day is allowed
     */
    @Transactional
    public CheckinResponse checkin(UUID userChallengeId) {
        UserChallenge uc = findUserChallengeByIdForCurrentUser(userChallengeId);
        UUID userId = uc.getUserId();

        if (!"IN_PROGRESS".equals(uc.getStatus())) {
            throw new ChallengeException(
                    "This challenge has ended (status: " + uc.getStatus() + "). Cannot check in.",
                    "CHALLENGE_NOT_ACTIVE");
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(uc.getStartDate())) {
            throw new ChallengeException(
                    "This challenge has not started yet. Start date: " + uc.getStartDate(),
                    "NOT_STARTED");
        }
        if (today.isAfter(uc.getEndDate())) {
            throw new ChallengeException(
                    "This challenge has expired. End date: " + uc.getEndDate(),
                    "CHALLENGE_EXPIRED");
        }
        if (checkinRepository.existsByUserChallengeIdAndCheckinDate(userChallengeId, today)) {
            throw new ChallengeException(
                    "You have already checked in today. Come back tomorrow!",
                    "ALREADY_CHECKED_IN");
        }

        Challenge challenge = challengeRepository.findById(uc.getChallengeId())
                .orElseThrow(() -> new ChallengeException(
                        "Challenge data error.", "DATA_ERROR"));

        // Evaluate whether today's food data meets the challenge criteria
        EvalResult eval = evaluateToday(userId, challenge);

        // Persist the check-in record
        ChallengeCheckin checkin = new ChallengeCheckin(
                userChallengeId, userId, today,
                eval.passed, eval.actualValue, eval.note);
        try {
            checkinRepository.save(checkin);
        } catch (DataIntegrityViolationException e) {
            // Unique constraint on (user_challenge_id, checkin_date) — duplicate check-in
            throw new ChallengeException(
                    "You have already checked in today. Come back tomorrow!",
                    "ALREADY_CHECKED_IN");
        }

        // Update progress counters
        if (eval.passed) {
            uc.setCompletedDays(uc.getCompletedDays() + 1);
        }
        uc.setCurrentStreak(calculateStreak(userChallengeId));

        // Check whether the whole challenge is now complete
        boolean challengeCompleted = uc.getCompletedDays() >= challenge.getDurationDays();
        if (challengeCompleted) {
            uc.setStatus("COMPLETED");
            uc.setCompletedAt(LocalDateTime.now());
        }

        userChallengeRepository.save(uc);

        String message = buildEncouragementMessage(eval.passed, uc, challenge, challengeCompleted);

        return new CheckinResponse(
                today, eval.passed, eval.actualValue, eval.note,
                uc.getCompletedDays(), uc.getCurrentStreak(),
                challengeCompleted, message, eval.nutritionSnapshot);
    }

    /**
     * Read-only preview of today's metrics and check-in status for one challenge.
     * Does NOT create a check-in record. Useful for frontend dashboard cards.
     */
    public TodayStatusResponse getTodayStatus(UUID userChallengeId) {
        UserChallenge uc = findUserChallengeByIdForCurrentUser(userChallengeId);
        UUID userId = uc.getUserId();

        Challenge challenge = challengeRepository.findById(uc.getChallengeId())
                .orElseThrow(() -> new ChallengeException("Challenge data error.", "DATA_ERROR"));

        LocalDate today = LocalDate.now();
        int daysRemaining = (int) java.time.temporal.ChronoUnit.DAYS.between(today, uc.getEndDate());
        int progressPercent = challenge.getDurationDays() > 0
                ? (int) Math.min(100, uc.getCompletedDays() * 100.0 / challenge.getDurationDays())
                : 0;

        // Check if already checked in today
        Optional<ChallengeCheckin> existingCheckin =
                checkinRepository.findByUserChallengeIdAndCheckinDate(userChallengeId, today);
        if (existingCheckin.isPresent()) {
            // Already checked in — still evaluate for the snapshot so frontend has current data
            ChallengeCheckin ci = existingCheckin.get();
            EvalResult snapshot = evaluateToday(userId, challenge);
            return new TodayStatusResponse(
                    true, ci.isPassed(), ci.getActualValue(),
                    challenge.getTargetValue(), ci.getNote(),
                    daysRemaining, uc.getCurrentStreak(), progressPercent,
                    snapshot.nutritionSnapshot);
        }

        // Not yet checked in — evaluate current metrics without persisting
        EvalResult preview = evaluateToday(userId, challenge);
        return new TodayStatusResponse(
                false, null, preview.actualValue,
                challenge.getTargetValue(), preview.note,
                daysRemaining, uc.getCurrentStreak(), progressPercent,
                preview.nutritionSnapshot);
    }

    /** Abandon an in-progress challenge. Completed or already-abandoned challenges cannot be abandoned. */
    @Transactional
    public void abandonChallenge(UUID userChallengeId) {
        UserChallenge uc = findUserChallengeByIdForCurrentUser(userChallengeId);

        if (!"IN_PROGRESS".equals(uc.getStatus())) {
            throw new ChallengeException(
                    "This challenge is not in progress (current status: " + uc.getStatus() + "). Cannot abandon.",
                    "CHALLENGE_NOT_ACTIVE");
        }

        uc.setStatus("ABANDONED");
        userChallengeRepository.save(uc);
    }

    // =========================================================================
    // Private: daily nutrition evaluation
    // =========================================================================

    /**
     * Reads ALL of today's logs from the database (food, water, exercise) and
     * evaluates whether the user met the challenge criteria.
     *
     * Always collects the full NutritionSnapshot regardless of challenge type,
     * so the frontend can display any combination of metrics it needs.
     */
    private EvalResult evaluateToday(UUID userId, Challenge challenge) {
        LocalDate today = LocalDate.now();

        // --- Always fetch all data sources ---

        // Water
        double totalWater = waterLogService.getTodayTotalMl(userId);

        // Exercise
        double caloriesBurned = exerciseLogService.getTodayCaloriesBurned(userId);

        // Food logs
        List<FoodLog> logs = foodLogRepository.findByUserIdAndLogDate(userId, today);
        int logCount = logs.size();

        // Flatten all nutrition items from all food logs
        List<NutritionData> allNutrition = logs.stream()
                .flatMap(log -> nutritionDataRepository.findByFoodLogId(log.getId()).stream())
                .collect(Collectors.toList());

        double totalCalories = allNutrition.stream()
                .mapToDouble(n -> n.getCalories() != null ? n.getCalories() : 0.0).sum();
        double totalProtein = allNutrition.stream()
                .mapToDouble(n -> n.getProteinG() != null ? n.getProteinG() : 0.0).sum();
        double totalFat = allNutrition.stream()
                .mapToDouble(n -> n.getFatG() != null ? n.getFatG() : 0.0).sum();
        double totalCarbs = allNutrition.stream()
                .mapToDouble(n -> n.getCarbsG() != null ? n.getCarbsG() : 0.0).sum();
        double totalFiber = allNutrition.stream()
                .mapToDouble(n -> n.getFiberG() != null ? n.getFiberG() : 0.0).sum();

        NutritionSnapshot snapshot = new NutritionSnapshot(
                totalCalories, totalProtein, totalFat, totalCarbs,
                totalFiber, totalWater, caloriesBurned, logCount);

        // --- Evaluate the challenge-specific criterion ---

        switch (challenge.getType()) {
            case "DAILY_WATER": {
                double target = challenge.getTargetValue();
                boolean passed = totalWater >= target;
                String note = String.format(
                        "Today's water intake: %.0f ml (target: %.0f ml, ~%.1f cups) %s",
                        totalWater, target, totalWater / 237.0, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, totalWater, note, snapshot);
            }
            case "CALORIE_BURN": {
                double target = challenge.getTargetValue();
                boolean passed = caloriesBurned >= target;
                String note = String.format(
                        "Today's calories burned: %.0f kcal (target: %.0f kcal) %s",
                        caloriesBurned, target, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, caloriesBurned, note, snapshot);
            }
            case "CALORIE_CONTROL": {
                double target = getUserCalorieTarget(userId);
                double low    = target * 0.8;
                double high   = target * 1.1;
                boolean passed = logCount > 0 && totalCalories >= low && totalCalories <= high;
                String note = String.format(
                        "Today's calories: %.0f kcal (target: %.0f kcal, allowed range: %.0f-%.0f kcal) %s",
                        totalCalories, target, low, high, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, totalCalories, note, snapshot);
            }
            case "PROTEIN_CHAMPION": {
                double target  = challenge.getTargetValue();
                boolean passed = totalProtein >= target;
                String note = String.format(
                        "Today's protein: %.1f g (target >= %.0f g) %s",
                        totalProtein, target, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, totalProtein, note, snapshot);
            }
            case "LOG_STREAK": {
                boolean passed = logCount >= 1;
                String note = String.format(
                        "Today's food log entries: %d %s",
                        logCount, passed ? "\u2705" : "\u274c (remember to log your meals!)");
                return new EvalResult(passed, logCount, note, snapshot);
            }
            case "LOW_CARB": {
                double limit   = challenge.getTargetValue();
                boolean passed = logCount > 0 && totalCarbs <= limit;
                String note = String.format(
                        "Today's carbs: %.1f g (target <= %.0f g) %s",
                        totalCarbs, limit, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, totalCarbs, note, snapshot);
            }
            case "LIGHT_EATER": {
                double limit   = challenge.getTargetValue();
                boolean passed = logCount > 0 && totalCalories <= limit;
                String note = String.format(
                        "Today's calories: %.0f kcal (target <= %.0f kcal) %s",
                        totalCalories, limit, passed ? "\u2705" : "\u274c");
                return new EvalResult(passed, totalCalories, note, snapshot);
            }
            default:
                return new EvalResult(false, 0.0, "Unknown challenge type — cannot evaluate.", snapshot);
        }
    }

    /**
     * Reads the user's daily calorie target from their profile.
     * Falls back to 2000 kcal if not set (used by CALORIE_CONTROL challenges).
     */
    private double getUserCalorieTarget(UUID userId) {
        try {
            User user = userService.getUserById(userId.toString());
            if (user.getDailyCalorieTarget() != null && user.getDailyCalorieTarget() > 0) {
                return user.getDailyCalorieTarget();
            }
        } catch (Exception ignored) {
            // User not found or target not set — fall through to default
        }
        return 2000.0;
    }

    /**
     * Calculates the current consecutive streak by walking backwards from today
     * through check-in records. Stops as soon as a day is missed or failed.
     */
    private int calculateStreak(UUID userChallengeId) {
        List<ChallengeCheckin> checkins = checkinRepository
                .findByUserChallengeIdOrderByCheckinDateDesc(userChallengeId);

        int streak = 0;
        LocalDate expected = LocalDate.now();
        for (ChallengeCheckin c : checkins) {
            if (c.getCheckinDate().equals(expected) && c.isPassed()) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    /** Builds a motivational message to display after a check-in. */
    private String buildEncouragementMessage(boolean passed, UserChallenge uc,
                                              Challenge challenge, boolean completed) {
        if (completed) {
            return String.format(
                    "🎉 Congratulations! You completed the \"%s\" challenge! " +
                    "%d days achieved — amazing job!",
                    challenge.getTitle(), uc.getCompletedDays());
        }
        int remaining = challenge.getDurationDays() - uc.getCompletedDays();
        if (passed) {
            if (uc.getCurrentStreak() >= 3) {
                return String.format(
                        "🔥 %d-day streak! Only %d more days to finish — keep it up!",
                        uc.getCurrentStreak(), remaining);
            }
            return String.format(
                    "✅ Goal met today! %d/%d days completed — keep going!",
                    uc.getCompletedDays(), challenge.getDurationDays());
        } else {
            return String.format(
                    "💪 Missed today's goal, but that's okay! %d more days to go — " +
                    "remember to log your meals before checking in!",
                    remaining);
        }
    }

    // =========================================================================
    // DTO mapping
    // =========================================================================

    private ChallengeResponse toChallengeResponse(Challenge c) {
        return new ChallengeResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getType(),
                c.getDurationDays(), c.getTargetValue(), c.getBadgeIcon());
    }

    private UserChallengeResponse toUserChallengeResponse(UserChallenge uc,
                                                           Challenge challenge,
                                                           List<CheckinResponse> checkins) {
        ChallengeResponse cr = challenge != null ? toChallengeResponse(challenge) : null;
        return new UserChallengeResponse(
                uc.getId(), uc.getUserId(), uc.getProfileId(), cr, uc.getStatus(),
                uc.getStartDate(), uc.getEndDate(),
                uc.getCompletedDays(), uc.getCurrentStreak(),
                uc.getJoinedAt(), uc.getCompletedAt(),
                checkins);
    }

    // =========================================================================
    // Internal data class for evaluation results
    // =========================================================================

    private static class EvalResult {
        final boolean passed;
        final double  actualValue;
        final String  note;
        final NutritionSnapshot nutritionSnapshot;

        EvalResult(boolean passed, double actualValue, String note, NutritionSnapshot nutritionSnapshot) {
            this.passed            = passed;
            this.actualValue       = actualValue;
            this.note              = note;
            this.nutritionSnapshot = nutritionSnapshot;
        }
    }
}
