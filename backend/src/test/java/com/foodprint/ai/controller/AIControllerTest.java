package com.foodprint.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.FoodAnalysisResult.MealSummary;
import com.foodprint.ai.service.AIRequestRouter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Isolated tests for AIController.
 * Uses @WebMvcTest — only loads the web layer, mocks the router.
 */
@WebMvcTest(AIController.class)
@AutoConfigureMockMvc(addFilters = false)
class AIControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AIRequestRouter router;

        @MockBean
        private com.foodprint.ai.service.AIMealLogService aiMealLogService;

        @Autowired
        private ObjectMapper objectMapper;

        private AIResponse mockFoodAnalysisResponse() {
                MealSummary summary = new MealSummary();
                summary.setTotalCalories(500.0);
                summary.setFoodCount(2);
                FoodAnalysisResult result = new FoodAnalysisResult(List.of(), summary, 0.9);
                return AIResponse.foodAnalysis(result);
        }

        @Test
        @DisplayName("POST /api/ai/analyze/image - successful image analysis")
        void analyzeImage_success() throws Exception {
                when(router.routeImage(any(), anyString())).thenReturn(mockFoodAnalysisResponse());

                MockMultipartFile imageFile = new MockMultipartFile(
                                "image", "food.jpg", "image/jpeg", "fake-image".getBytes());

                mockMvc.perform(multipart("/api/ai/analyze/image").file(imageFile))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.type").value("FOOD_ANALYSIS"))
                                .andExpect(jsonPath("$.data.foodAnalysis.summary.totalCalories").value(500));
        }

        @Test
        @DisplayName("POST /api/ai/analyze/image - empty image returns 400")
        void analyzeImage_emptyFile() throws Exception {
                MockMultipartFile emptyFile = new MockMultipartFile(
                                "image", "food.jpg", "image/jpeg", new byte[0]);

                mockMvc.perform(multipart("/api/ai/analyze/image").file(emptyFile))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/ai/analyze/image - non-image file returns 400")
        void analyzeImage_nonImageFile() throws Exception {
                MockMultipartFile textFile = new MockMultipartFile(
                                "image", "food.txt", "text/plain", "not an image".getBytes());

                mockMvc.perform(multipart("/api/ai/analyze/image").file(textFile))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/ai/analyze - successful text analysis")
        void analyzeText_success() throws Exception {
                when(router.routeText(anyString(), any())).thenReturn(mockFoodAnalysisResponse());

                String body = """
                                { "text": "I had chicken and rice for lunch" }
                                """;

                mockMvc.perform(post("/api/ai/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.type").value("FOOD_ANALYSIS"));
        }

        @Test
        @DisplayName("POST /api/ai/analyze - blank text returns 400")
        void analyzeText_blankText() throws Exception {
                String body = """
                                { "text": "" }
                                """;

                mockMvc.perform(post("/api/ai/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/ai/analyze - diet advice with user context")
        void analyzeText_dietAdvice() throws Exception {
                when(router.routeText(anyString(), any())).thenReturn(AIResponse.dietAdvice("Eat more protein!"));

                String body = """
                                {
                                  "text": "I want to lose weight",
                                  "userContext": {
                                    "heightCm": 170,
                                    "weightKg": 70,
                                    "goal": "LOSE_WEIGHT"
                                  }
                                }
                                """;

                mockMvc.perform(post("/api/ai/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.code").value(200))
                                .andExpect(jsonPath("$.data.type").value("DIET_ADVICE"))
                                .andExpect(jsonPath("$.data.adviceText").value("Eat more protein!"));
        }
}
