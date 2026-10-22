package com.foodprint.ai.service;

import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.UserNutritionContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Isolated tests for DietAdviceService.
 */
@ExtendWith(MockitoExtension.class)
class DietAdviceServiceTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private DietAdviceService dietAdviceService;

    private UserNutritionContext createCompleteContext() {
        UserNutritionContext ctx = new UserNutritionContext();
        ctx.setHeightCm(170.0);
        ctx.setWeightKg(70.0);
        ctx.setAge(25);
        ctx.setGender("male");
        ctx.setGoal("LOSE_WEIGHT");
        ctx.setDailyCalorieTarget(1800);
        ctx.setTodayCalories(1200.0);
        ctx.setTodayProtein(80.0);
        ctx.setTodayFat(40.0);
        ctx.setTodayCarbs(120.0);
        return ctx;
    }

    @Test
    @DisplayName("Complete profile - returns diet advice")
    void generateAdvice_completeProfile() {
        when(geminiService.generateDietAdvice(any())).thenReturn("Eat more protein!");

        AIResponse response = dietAdviceService.generateAdvice(createCompleteContext());

        assertEquals("DIET_ADVICE", response.getType());
        assertEquals("Eat more protein!", response.getAdviceText());
        assertFalse(response.isProfileIncomplete());
    }

    @Test
    @DisplayName("Incomplete profile - returns profile needed")
    void generateAdvice_incompleteProfile() {
        UserNutritionContext ctx = new UserNutritionContext();
        ctx.setHeightCm(170.0);
        // Missing weight and goal

        AIResponse response = dietAdviceService.generateAdvice(ctx);

        assertEquals("PROFILE_NEEDED", response.getType());
        assertTrue(response.isProfileIncomplete());
        assertNotNull(response.getProfilePrompt());
        verify(geminiService, never()).generateDietAdvice(any());
    }

    @Test
    @DisplayName("Null context - returns profile needed")
    void generateAdvice_nullContext() {
        AIResponse response = dietAdviceService.generateAdvice(null);

        assertTrue(response.isProfileIncomplete());
        verify(geminiService, never()).generateDietAdvice(any());
    }

    @Test
    @DisplayName("Generate plan - complete profile")
    void generatePlan_completeProfile() {
        when(geminiService.generateDietAdvice(any())).thenReturn("Monday: oatmeal...");

        AIResponse response = dietAdviceService.generatePlan(createCompleteContext());

        assertEquals("DIET_PLAN", response.getType());
        assertNotNull(response.getAdviceText());
    }

    @Test
    @DisplayName("Generate plan - incomplete profile")
    void generatePlan_incompleteProfile() {
        AIResponse response = dietAdviceService.generatePlan(new UserNutritionContext());

        assertTrue(response.isProfileIncomplete());
    }
}
