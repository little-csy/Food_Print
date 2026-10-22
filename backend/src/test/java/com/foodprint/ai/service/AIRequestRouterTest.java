package com.foodprint.ai.service;

import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.FoodAnalysisResult.MealSummary;
import com.foodprint.ai.dto.UserNutritionContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Isolated tests for AIRequestRouter.
 * Mocks all downstream services.
 */
@ExtendWith(MockitoExtension.class)
class AIRequestRouterTest {

    @Mock
    private GeminiService geminiService;
    @Mock
    private FoodAnalysisService foodAnalysisService;
    @Mock
    private DietAdviceService dietAdviceService;
    @Mock
    private ChatService chatService;

    @InjectMocks
    private AIRequestRouter router;

    private FoodAnalysisResult mockFoodResult() {
        MealSummary summary = new MealSummary();
        summary.setTotalCalories(500.0);
        summary.setFoodCount(2);
        return new FoodAnalysisResult(List.of(), summary, 0.9);
    }

    @Test
    @DisplayName("Image input - goes directly to food analysis, skips intent classification")
    void routeImage_directToFoodAnalysis() {
        when(foodAnalysisService.analyzeFromImage(any(), anyString())).thenReturn(mockFoodResult());

        AIResponse response = router.routeImage(new byte[] { 1, 2, 3 }, "image/jpeg");

        assertEquals("FOOD_ANALYSIS", response.getType());
        assertNotNull(response.getFoodAnalysis());
        verify(geminiService, never()).classifyIntent(anyString());
    }

    @Test
    @DisplayName("Text FOOD_LOG intent - routes to food analysis")
    void routeText_foodLog() {
        when(geminiService.classifyIntent(anyString())).thenReturn(UserIntent.FOOD_LOG);
        when(foodAnalysisService.analyzeFromText(anyString())).thenReturn(mockFoodResult());

        AIResponse response = router.routeText("I had chicken for lunch", null);

        assertEquals("FOOD_ANALYSIS", response.getType());
        verify(foodAnalysisService).analyzeFromText(anyString());
    }

    @Test
    @DisplayName("Text DIET_ADVICE intent - routes to diet advice")
    void routeText_dietAdvice() {
        when(geminiService.classifyIntent(anyString())).thenReturn(UserIntent.DIET_ADVICE);
        when(dietAdviceService.generateAdvice(any())).thenReturn(AIResponse.dietAdvice("Eat more veggies!"));

        UserNutritionContext ctx = new UserNutritionContext();
        AIResponse response = router.routeText("How should I eat?", ctx);

        assertEquals("DIET_ADVICE", response.getType());
        verify(dietAdviceService).generateAdvice(ctx);
    }

    @Test
    @DisplayName("Text DIET_PLAN intent - routes to diet plan")
    void routeText_dietPlan() {
        when(geminiService.classifyIntent(anyString())).thenReturn(UserIntent.DIET_PLAN);
        when(dietAdviceService.generatePlan(any())).thenReturn(AIResponse.dietPlan("Week 1: ..."));

        AIResponse response = router.routeText("Make me a meal plan", new UserNutritionContext());

        assertEquals("DIET_PLAN", response.getType());
    }

    @Test
    @DisplayName("Text GENERAL_CHAT intent - routes to chat")
    void routeText_generalChat() {
        when(geminiService.classifyIntent(anyString())).thenReturn(UserIntent.GENERAL_CHAT);
        when(chatService.chat(anyString(), any())).thenReturn("Hello! How can I help?");

        AIResponse response = router.routeText("I'm tired today", null);

        assertEquals("CHAT", response.getType());
        assertEquals("Hello! How can I help?", response.getAdviceText());
    }
}
