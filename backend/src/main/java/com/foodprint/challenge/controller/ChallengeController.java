package com.foodprint.challenge.controller;

import com.foodprint.challenge.dto.ChallengeCreateRequest;
import com.foodprint.challenge.dto.ChallengeResponse;
import com.foodprint.challenge.dto.CheckinResponse;
import com.foodprint.challenge.dto.TodayStatusResponse;
import com.foodprint.challenge.dto.UserChallengeResponse;
import com.foodprint.challenge.exception.ChallengeException;
import com.foodprint.challenge.service.ChallengeService;
import com.foodprint.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Challenge module REST API.
 *
 * All responses follow the unified format: { "code": 200/400/500, "message": "...", "data": ... }
 *
 * Public (no auth required):
 *   GET  /api/challenges
 *
 * Authenticated (Authorization: Bearer &lt;supabase-jwt&gt; header required):
 *   POST   /api/challenges/{challengeId}/join
 *   GET    /api/challenges/my
 *   GET    /api/challenges/my/active
 *   GET    /api/challenges/my/{userChallengeId}
 *   GET    /api/challenges/my/{userChallengeId}/today-status
 *   POST   /api/challenges/my/{userChallengeId}/checkin
 *   DELETE /api/challenges/my/{userChallengeId}
 */
@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    // -------------------------------------------------------------------------
    // Business error handler — returns HTTP 400 for all ChallengeExceptions
    // -------------------------------------------------------------------------

    @ExceptionHandler(ChallengeException.class)
    public ResponseEntity<ApiResponse<Void>> handleChallengeError(ChallengeException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
    }

    // -------------------------------------------------------------------------
    // Public
    // -------------------------------------------------------------------------

    /**
     * Returns all system-defined challenge templates.
     * No authentication required — suitable for a public "Challenge Hall" screen.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChallengeResponse>>> getAllChallenges() {
        return ResponseEntity.ok(ApiResponse.success(challengeService.getAllChallenges()));
    }

    /**
     * Creates a new custom challenge template.
     * Authentication required.
     *
     * Request body:
     *   title        (required) — display name
     *   description  (optional) — full description
     *   type         (required) — unique type key, e.g. "MY_CUSTOM"
     *   duration_days (required) — number of days, must be >= 1
     *   target_value (optional, default 0.0)
     *   badge_icon   (optional, default "🏆")
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChallengeResponse>> createChallenge(
            @RequestBody ChallengeCreateRequest request) {
        ChallengeResponse response = challengeService.createChallenge(request);
        return ResponseEntity.ok(ApiResponse.success("Challenge created successfully!", response));
    }

    // -------------------------------------------------------------------------
    // Join
    // -------------------------------------------------------------------------

    /**
     * Join a challenge (or re-join after completion / abandonment).
     * Returns 400 ALREADY_IN_PROGRESS if the user already has an active run.
     *
     * @param challengeId  Challenge template ID (from GET /api/challenges)
     */
    @PostMapping("/{challengeId}/join")
    public ResponseEntity<ApiResponse<UserChallengeResponse>> joinChallenge(
            @PathVariable UUID challengeId) {

        UserChallengeResponse response = challengeService.joinChallenge(challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge joined successfully!", response));
    }

    // -------------------------------------------------------------------------
    // My challenges
    // -------------------------------------------------------------------------

    /**
     * Returns all of the user's challenge records across all statuses (IN_PROGRESS, COMPLETED, ABANDONED).
     * Check-in history is omitted to keep the payload small — use the detail endpoint for that.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getMyChallenges() {

        return ResponseEntity.ok(ApiResponse.success(challengeService.getMyChallenges()));
    }

    /**
     * Returns only the user's currently in-progress challenges.
     * Useful for displaying active challenge progress on the home screen.
     */
    @GetMapping("/my/active")
    public ResponseEntity<ApiResponse<List<UserChallengeResponse>>> getMyActiveChallenges() {

        return ResponseEntity.ok(
                ApiResponse.success(challengeService.getMyActiveChallenges()));
    }

    /**
     * Returns full detail for a single challenge record, including the complete check-in history
     * sorted by date ascending.
     *
     * @param userChallengeId  The UserChallenge ID (obtained from /my or /my/active)
     */
    @GetMapping("/my/{userChallengeId}")
    public ResponseEntity<ApiResponse<UserChallengeResponse>> getUserChallengeDetail(
            @PathVariable UUID userChallengeId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        challengeService.getUserChallengeDetail(userChallengeId)));
    }

    /**
     * Read-only preview of today's metrics and check-in status for one active challenge.
     * Does NOT create a check-in record — purely informational.
     * Returns: checkedIn, passed (if already checked in), currentValue, targetValue, summary,
     *          daysRemaining, currentStreak, progressPercent.
     */
    @GetMapping("/my/{userChallengeId}/today-status")
    public ResponseEntity<ApiResponse<TodayStatusResponse>> getTodayStatus(
            @PathVariable UUID userChallengeId) {
        return ResponseEntity.ok(
                ApiResponse.success(challengeService.getTodayStatus(userChallengeId)));
    }

    /**
     * Daily check-in.
     *
     * The server automatically reads the user's food logs / water logs / exercise logs for today,
     * calculates the relevant metric, and evaluates it against the challenge criteria.
     * The result (passed / failed) and an encouragement message are returned.
     *
     * Rules:
     *  - One check-in per day (duplicate returns 400)
     *  - Log your data first (food / water / exercise), then call this endpoint
     *  - LOG_STREAK only requires at least one food log entry; other types need specific data
     */
    @PostMapping("/my/{userChallengeId}/checkin")
    public ResponseEntity<ApiResponse<CheckinResponse>> checkin(
            @PathVariable UUID userChallengeId) {

        CheckinResponse response = challengeService.checkin(userChallengeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Alias for checkin without /my/ path — backward compatibility.
     * Preferred path: POST /api/challenges/my/{userChallengeId}/checkin
     */
    @PostMapping("/{userChallengeId}/checkin")
    public ResponseEntity<ApiResponse<CheckinResponse>> checkinAlias(
            @PathVariable UUID userChallengeId) {

        CheckinResponse response = challengeService.checkin(userChallengeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Abandon an in-progress challenge.
     * Returns 400 if the challenge is already completed or abandoned.
     */
    @DeleteMapping("/my/{userChallengeId}")
    public ResponseEntity<ApiResponse<Void>> abandonChallenge(
            @PathVariable UUID userChallengeId) {

        challengeService.abandonChallenge(userChallengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge abandoned successfully.", null));
    }
}
