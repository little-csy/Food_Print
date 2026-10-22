package com.foodprint.water.service;

import com.foodprint.water.dto.WaterLogRequest;
import com.foodprint.water.dto.WaterTodayResponse;
import com.foodprint.water.entity.WaterLog;
import com.foodprint.water.repository.WaterLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WaterLogService {

    /** Default daily water goal in ml (≈ 8 cups) */
    private static final double DEFAULT_GOAL_ML = 2000.0;

    @Autowired
    private WaterLogRepository waterLogRepository;

    // -------------------------------------------------------------------------

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(auth.getName());
    }

    // -------------------------------------------------------------------------

    /** Log a water intake event for the authenticated user (defaults to today). */
    public WaterTodayResponse logWater(WaterLogRequest request) {
        if (request.getAmountMl() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0 ml.");
        }
        UUID userId = getCurrentUserId();
        WaterLog log = new WaterLog(userId, LocalDate.now(), request.getAmountMl(), request.getNote());
        waterLogRepository.save(log);
        return buildTodayResponse(userId);
    }

    /** Today's water summary for the authenticated user. */
    public WaterTodayResponse getTodayIntake() {
        return buildTodayResponse(getCurrentUserId());
    }

    /**
     * Returns the total water consumed today (ml) for a given user ID.
     * Called internally by ChallengeService during check-in evaluation.
     */
    public double getTodayTotalMl(UUID userId) {
        return waterLogRepository.sumAmountMlByUserIdAndLogDate(userId, LocalDate.now());
    }

    // -------------------------------------------------------------------------

    private WaterTodayResponse buildTodayResponse(UUID userId) {
        LocalDate today = LocalDate.now();
        double totalMl = waterLogRepository.sumAmountMlByUserIdAndLogDate(userId, today);
        List<WaterTodayResponse.WaterLogEntry> entries =
                waterLogRepository.findByUserIdAndLogDateOrderByCreatedAtAsc(userId, today)
                        .stream()
                        .map(w -> new WaterTodayResponse.WaterLogEntry(
                                w.getId(), w.getAmountMl(), w.getNote(), w.getCreatedAt()))
                        .collect(Collectors.toList());
        return new WaterTodayResponse(totalMl, DEFAULT_GOAL_ML, entries);
    }
}
