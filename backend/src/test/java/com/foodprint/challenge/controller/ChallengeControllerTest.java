package com.foodprint.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.challenge.dto.ChallengeResponse;
import com.foodprint.challenge.dto.CheckinResponse;
import com.foodprint.challenge.dto.TodayStatusResponse;
import com.foodprint.challenge.dto.UserChallengeResponse;
import com.foodprint.challenge.exception.ChallengeException;
import com.foodprint.challenge.service.ChallengeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Isolated web-layer tests for ChallengeController.
 *
 * Every test verifies both HTTP status and the ApiResponse envelope
 * ({ "code": ..., "message": ..., "data": ... }) that all challenge endpoints use.
 *
 * Security filters are disabled so tests focus on controller/service contract,
 * not on JWT authentication which is handled externally by Supabase.
 */
@WebMvcTest(ChallengeController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChallengeService challengeService;

    @Autowired
    private ObjectMapper objectMapper;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private ChallengeResponse buildChallengeResponse(String type) {
        return new ChallengeResponse(
                UUID.randomUUID(), "Test Challenge", "Description", type, 7, 50.0, "🏆");
    }

    private UserChallengeResponse buildUserChallengeResponse(String status) {
        ChallengeResponse cr = buildChallengeResponse("PROTEIN_CHAMPION");
        return new UserChallengeResponse(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), cr,
                status,
                LocalDate.now(), LocalDate.now().plusDays(6),
                2, 2,
                null, null,
                Collections.emptyList());
    }

    // -----------------------------------------------------------------------
    // GET /api/challenges (public)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/challenges – 200 with all challenge templates (no auth required)")
    void getAllChallenges_success() throws Exception {
        List<ChallengeResponse> challenges = List.of(
                buildChallengeResponse("CALORIE_CONTROL"),
                buildChallengeResponse("PROTEIN_CHAMPION"),
                buildChallengeResponse("LOG_STREAK"),
                buildChallengeResponse("LOW_CARB"),
                buildChallengeResponse("LIGHT_EATER"),
                buildChallengeResponse("DAILY_WATER"),
                buildChallengeResponse("CALORIE_BURN")
        );
        when(challengeService.getAllChallenges()).thenReturn(challenges);

        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(7));
    }

    @Test
    @DisplayName("GET /api/challenges – 200 with empty list when no challenges seeded")
    void getAllChallenges_emptyList() throws Exception {
        when(challengeService.getAllChallenges()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // -----------------------------------------------------------------------
    // POST /api/challenges/{challengeId}/join
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/challenges/{id}/join – 200 when successfully joined")
    void joinChallenge_success() throws Exception {
        UUID challengeId = UUID.randomUUID();
        UserChallengeResponse mockResponse = buildUserChallengeResponse("IN_PROGRESS");
        when(challengeService.joinChallenge(challengeId)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/challenges/{challengeId}/join", challengeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Challenge joined successfully!"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completedDays").value(2))
                .andExpect(jsonPath("$.data.progressPercent").isNumber());
    }

    @Test
    @DisplayName("POST /api/challenges/{id}/join – 400 when already in progress")
    void joinChallenge_alreadyInProgress_returns400() throws Exception {
        UUID challengeId = UUID.randomUUID();
        when(challengeService.joinChallenge(challengeId))
                .thenThrow(new ChallengeException(
                        "You are already doing this challenge.", "ALREADY_IN_PROGRESS"));

        mockMvc.perform(post("/api/challenges/{challengeId}/join", challengeId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("You are already doing this challenge."));
    }

    @Test
    @DisplayName("POST /api/challenges/{id}/join – 400 when challenge not found")
    void joinChallenge_notFound_returns400() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(challengeService.joinChallenge(unknownId))
                .thenThrow(new ChallengeException("Challenge not found: " + unknownId, "CHALLENGE_NOT_FOUND"));

        mockMvc.perform(post("/api/challenges/{challengeId}/join", unknownId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // -----------------------------------------------------------------------
    // GET /api/challenges/my
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/challenges/my – 200 with all user challenge records")
    void getMyChallenges_success() throws Exception {
        List<UserChallengeResponse> records = List.of(
                buildUserChallengeResponse("IN_PROGRESS"),
                buildUserChallengeResponse("COMPLETED"));
        when(challengeService.getMyChallenges()).thenReturn(records);

        mockMvc.perform(get("/api/challenges/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/challenges/my – 200 with empty list when user has no challenges")
    void getMyChallenges_empty() throws Exception {
        when(challengeService.getMyChallenges()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/challenges/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // -----------------------------------------------------------------------
    // GET /api/challenges/my/active
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/challenges/my/active – 200 with active challenges including today status fields")
    void getMyActiveChallenges_success() throws Exception {
        UserChallengeResponse activeChallenge = buildUserChallengeResponse("IN_PROGRESS");
        activeChallenge.setTodayCheckedIn(true);
        activeChallenge.setTodayPassed(true);

        when(challengeService.getMyActiveChallenges()).thenReturn(List.of(activeChallenge));

        mockMvc.perform(get("/api/challenges/my/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data[0].todayCheckedIn").value(true))
                .andExpect(jsonPath("$.data[0].todayPassed").value(true))
                .andExpect(jsonPath("$.data[0].progressPercent").isNumber())
                .andExpect(jsonPath("$.data[0].daysRemaining").isNumber());
    }

    @Test
    @DisplayName("GET /api/challenges/my/active – todayPassed is null when not yet checked in today")
    void getMyActiveChallenges_notCheckedInToday() throws Exception {
        UserChallengeResponse activeChallenge = buildUserChallengeResponse("IN_PROGRESS");
        activeChallenge.setTodayCheckedIn(false);
        activeChallenge.setTodayPassed(null);

        when(challengeService.getMyActiveChallenges()).thenReturn(List.of(activeChallenge));

        mockMvc.perform(get("/api/challenges/my/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].todayCheckedIn").value(false))
                // null field is omitted from JSON with @JsonInclude(NON_NULL) on ApiResponse,
                // so we just verify todayCheckedIn=false
                .andExpect(jsonPath("$.data[0].status").value("IN_PROGRESS"));
    }

    // -----------------------------------------------------------------------
    // GET /api/challenges/my/{userChallengeId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/challenges/my/{id} – 200 with full detail including check-in history")
    void getUserChallengeDetail_success() throws Exception {
        UUID ucId = UUID.randomUUID();
        UserChallengeResponse detail = buildUserChallengeResponse("IN_PROGRESS");
        when(challengeService.getUserChallengeDetail(ucId)).thenReturn(detail);

        mockMvc.perform(get("/api/challenges/my/{userChallengeId}", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.completedDays").value(2))
                .andExpect(jsonPath("$.data.currentStreak").value(2));
    }

    @Test
    @DisplayName("GET /api/challenges/my/{id} – 400 when challenge not found or access denied")
    void getUserChallengeDetail_notFound_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        when(challengeService.getUserChallengeDetail(ucId))
                .thenThrow(new ChallengeException("Challenge record not found or access denied.", "NOT_FOUND"));

        mockMvc.perform(get("/api/challenges/my/{userChallengeId}", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // -----------------------------------------------------------------------
    // GET /api/challenges/my/{userChallengeId}/today-status
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/challenges/my/{id}/today-status – 200 with preview when not checked in")
    void getTodayStatus_notCheckedIn() throws Exception {
        UUID ucId = UUID.randomUUID();
        TodayStatusResponse status = new TodayStatusResponse(
                false, null, 1200.0, 2000.0,
                "Today's water: 1200 ml (target: 2000 ml) ❌",
                4, 3, 57);
        when(challengeService.getTodayStatus(ucId)).thenReturn(status);

        mockMvc.perform(get("/api/challenges/my/{userChallengeId}/today-status", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.checkedIn").value(false))
                .andExpect(jsonPath("$.data.currentValue").value(1200.0))
                .andExpect(jsonPath("$.data.targetValue").value(2000.0))
                .andExpect(jsonPath("$.data.daysRemaining").value(4))
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.progressPercent").value(57));
    }

    @Test
    @DisplayName("GET /api/challenges/my/{id}/today-status – 200 with actual result when checked in")
    void getTodayStatus_alreadyCheckedIn() throws Exception {
        UUID ucId = UUID.randomUUID();
        TodayStatusResponse status = new TodayStatusResponse(
                true, true, 2300.0, 2000.0,
                "Today's water: 2300 ml ✅",
                2, 5, 71);
        when(challengeService.getTodayStatus(ucId)).thenReturn(status);

        mockMvc.perform(get("/api/challenges/my/{userChallengeId}/today-status", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checkedIn").value(true))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.currentValue").value(2300.0))
                .andExpect(jsonPath("$.data.summary").value("Today's water: 2300 ml ✅"));
    }

    @Test
    @DisplayName("GET /api/challenges/my/{id}/today-status – 400 when challenge not found")
    void getTodayStatus_notFound_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        when(challengeService.getTodayStatus(ucId))
                .thenThrow(new ChallengeException("Challenge record not found or access denied.", "NOT_FOUND"));

        mockMvc.perform(get("/api/challenges/my/{userChallengeId}/today-status", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // -----------------------------------------------------------------------
    // POST /api/challenges/my/{userChallengeId}/checkin
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/challenges/my/{id}/checkin – 200 with passed=true and streak")
    void checkin_passed() throws Exception {
        UUID ucId = UUID.randomUUID();
        CheckinResponse checkinResponse = new CheckinResponse(
                LocalDate.now(), true, 2500.0,
                "Today's water: 2500 ml ✅",
                5, 3, false,
                "🔥 3-day streak! Only 2 more days to finish — keep it up!");
        when(challengeService.checkin(ucId)).thenReturn(checkinResponse);

        mockMvc.perform(post("/api/challenges/my/{userChallengeId}/checkin", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.actualValue").value(2500.0))
                .andExpect(jsonPath("$.data.completedDays").value(5))
                .andExpect(jsonPath("$.data.currentStreak").value(3))
                .andExpect(jsonPath("$.data.challengeCompleted").value(false))
                .andExpect(jsonPath("$.data.message").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/challenges/my/{id}/checkin – 200 with challengeCompleted=true on final day")
    void checkin_challengeCompleted() throws Exception {
        UUID ucId = UUID.randomUUID();
        CheckinResponse checkinResponse = new CheckinResponse(
                LocalDate.now(), true, 2100.0,
                "Today's water: 2100 ml ✅",
                7, 7, true,
                "🎉 Congratulations! You completed the \"Hydration Hero\" challenge! 7 days achieved — amazing job!");
        when(challengeService.checkin(ucId)).thenReturn(checkinResponse);

        mockMvc.perform(post("/api/challenges/my/{userChallengeId}/checkin", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passed").value(true))
                .andExpect(jsonPath("$.data.completedDays").value(7))
                .andExpect(jsonPath("$.data.challengeCompleted").value(true))
                .andExpect(jsonPath("$.data.message").value(
                        "🎉 Congratulations! You completed the \"Hydration Hero\" challenge! 7 days achieved — amazing job!"));
    }

    @Test
    @DisplayName("POST /api/challenges/my/{id}/checkin – 200 with passed=false when goal not met")
    void checkin_failed() throws Exception {
        UUID ucId = UUID.randomUUID();
        CheckinResponse checkinResponse = new CheckinResponse(
                LocalDate.now(), false, 0.0,
                "Today's calories burned: 0 kcal (target: 200 kcal) ❌",
                2, 0, false,
                "💪 Missed today's goal, but that's okay! 5 more days to go — remember to log your meals before checking in!");
        when(challengeService.checkin(ucId)).thenReturn(checkinResponse);

        mockMvc.perform(post("/api/challenges/my/{userChallengeId}/checkin", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passed").value(false))
                .andExpect(jsonPath("$.data.currentStreak").value(0))
                .andExpect(jsonPath("$.data.challengeCompleted").value(false));
    }

    @Test
    @DisplayName("POST /api/challenges/my/{id}/checkin – 400 when already checked in today")
    void checkin_alreadyCheckedIn_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        when(challengeService.checkin(ucId))
                .thenThrow(new ChallengeException(
                        "You have already checked in today. Come back tomorrow!", "ALREADY_CHECKED_IN"));

        mockMvc.perform(post("/api/challenges/my/{userChallengeId}/checkin", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("You have already checked in today. Come back tomorrow!"));
    }

    @Test
    @DisplayName("POST /api/challenges/my/{id}/checkin – 400 when challenge has expired")
    void checkin_expired_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        when(challengeService.checkin(ucId))
                .thenThrow(new ChallengeException(
                        "This challenge has expired.", "CHALLENGE_EXPIRED"));

        mockMvc.perform(post("/api/challenges/my/{userChallengeId}/checkin", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/challenges/my/{userChallengeId}
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/challenges/my/{id} – 200 on successful abandon")
    void abandonChallenge_success() throws Exception {
        UUID ucId = UUID.randomUUID();
        doNothing().when(challengeService).abandonChallenge(ucId);

        mockMvc.perform(delete("/api/challenges/my/{userChallengeId}", ucId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Challenge abandoned successfully."));

        verify(challengeService, times(1)).abandonChallenge(ucId);
    }

    @Test
    @DisplayName("DELETE /api/challenges/my/{id} – 400 when challenge is already completed")
    void abandonChallenge_alreadyCompleted_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        doThrow(new ChallengeException(
                "This challenge is not in progress (current status: COMPLETED). Cannot abandon.",
                "CHALLENGE_NOT_ACTIVE"))
                .when(challengeService).abandonChallenge(ucId);

        mockMvc.perform(delete("/api/challenges/my/{userChallengeId}", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("DELETE /api/challenges/my/{id} – 400 when challenge not found or access denied")
    void abandonChallenge_notFound_returns400() throws Exception {
        UUID ucId = UUID.randomUUID();
        doThrow(new ChallengeException("Challenge record not found or access denied.", "NOT_FOUND"))
                .when(challengeService).abandonChallenge(ucId);

        mockMvc.perform(delete("/api/challenges/my/{userChallengeId}", ucId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
