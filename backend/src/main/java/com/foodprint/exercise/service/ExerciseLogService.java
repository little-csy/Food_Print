package com.foodprint.exercise.service;

import com.foodprint.exercise.dto.ExerciseLogRequest;
import com.foodprint.exercise.dto.ExerciseTodayResponse;
import com.foodprint.exercise.entity.ExerciseLog;
import com.foodprint.exercise.repository.ExerciseLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExerciseLogService {

    @Autowired
    private ExerciseLogRepository exerciseLogRepository;

    // -------------------------------------------------------------------------

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }

    // -------------------------------------------------------------------------

    /** Log an exercise session for the authenticated user (defaults to today). */
    public ExerciseTodayResponse logExercise(ExerciseLogRequest request) {
        if (request.getActivityType() == null || request.getActivityType().isBlank()) {
            throw new IllegalArgumentException("Activity type is required.");
        }
        if (request.getCaloriesBurned() <= 0) {
            throw new IllegalArgumentException("Calories burned must be greater than 0.");
        }
        UUID userId = getCurrentUserId();
        ExerciseLog log = new ExerciseLog(
                userId, LocalDate.now(),
                request.getActivityType(), request.getCaloriesBurned(),
                request.getDurationMin(), request.getNote());
        exerciseLogRepository.save(log);
        return buildTodayResponse(userId);
    }

    /** Today's exercise summary for the authenticated user. */
    public ExerciseTodayResponse getTodayExercise() {
        return buildTodayResponse(getCurrentUserId());
    }

    /**
     * Returns total calories burned today for a given user.
     * Called internally by ChallengeService during check-in evaluation.
     */
    public double getTodayCaloriesBurned(UUID userId) {
        return exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(userId, LocalDate.now());
    }

    // -------------------------------------------------------------------------

    private ExerciseTodayResponse buildTodayResponse(UUID userId) {
        LocalDate today = LocalDate.now();
        double totalCal = exerciseLogRepository.sumCaloriesBurnedByUserIdAndLogDate(userId, today);
        int totalMin = (int) exerciseLogRepository.sumDurationMinByUserIdAndLogDate(userId, today);
        List<ExerciseTodayResponse.ExerciseEntry> entries =
                exerciseLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(userId, today)
                        .stream()
                        .map(e -> new ExerciseTodayResponse.ExerciseEntry(
                                e.getId(), e.getActivityType(), e.getCaloriesBurned(),
                                e.getDurationMin(), e.getNote(), e.getCreatedAt()))
                        .collect(Collectors.toList());
        return new ExerciseTodayResponse(totalCal, totalMin, entries);
    }
}
