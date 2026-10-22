package com.foodprint.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.dto.FoodIdentificationResult;
import com.foodprint.ai.exception.AIServiceException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Isolated tests for GeminiService.
 * Uses MockWebServer — no real API calls.
 */
class GeminiServiceTest {

    private MockWebServer mockServer;
    private GeminiService geminiService;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        AIConfig config = new AIConfig();
        AIConfig.GeminiConfig geminiConfig = new AIConfig.GeminiConfig();
        geminiConfig.setApiKey("test-api-key");
        geminiConfig.setModel("gemini-2.5-flash");
        geminiConfig.setBaseUrl(mockServer.url("/v1beta").toString());
        geminiConfig.setTimeoutSeconds(5);
        config.setGemini(geminiConfig);

        WebClient webClient = WebClient.builder()
                .baseUrl(mockServer.url("/v1beta").toString())
                .build();

        geminiService = new GeminiService(webClient, config, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private String wrapInGeminiResponse(String text) {
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": %s }
                        ]
                      }
                    }
                  ]
                }
                """.formatted(com.fasterxml.jackson.core.io.JsonStringEncoder.getInstance() != null
                ? "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
                : "\"" + text + "\"");
    }

    private String wrapJsonInGeminiResponse(String jsonContent) {
        // Wrap a JSON string as text field inside Gemini's response structure
        String escaped = jsonContent.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          { "text": "%s" }
                        ]
                      }
                    }
                  ]
                }
                """.formatted(escaped);
    }

    @Test
    @DisplayName("Text food identification - parses multi-food result correctly")
    void identifyFoodFromText_success() throws Exception {
        String foodJson = loadTestResource("gemini_response_text1.json");
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse(foodJson))
                .addHeader("Content-Type", "application/json"));

        FoodIdentificationResult result = geminiService.identifyFoodFromText(
                "For lunch today I had a grilled chicken breast with one bowl of white rice");

        assertNotNull(result);
        assertEquals(5, result.getFoods().size());
        assertEquals("grilled chicken breast", result.getFoods().get(0).getNameEn());
        assertEquals("Grilled chicken breast", result.getFoods().get(0).getNameZh());
        assertEquals(120, result.getFoods().get(0).getPortionAmount());
        assertTrue(result.getConfidence() > 0.8);
    }

    @Test
    @DisplayName("Image food identification - parses result correctly")
    void identifyFoodFromImage_success() throws Exception {
        String foodJson = loadTestResource("gemini_response_food1.json");
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse(foodJson))
                .addHeader("Content-Type", "application/json"));

        byte[] fakeImage = "fake-image-data".getBytes(StandardCharsets.UTF_8);
        FoodIdentificationResult result = geminiService.identifyFoodFromImage(fakeImage, "image/jpeg");

        assertNotNull(result);
        assertEquals(3, result.getFoods().size());
        assertEquals("udon noodles", result.getFoods().get(0).getNameEn());
        assertEquals("Udon noodles", result.getFoods().get(0).getNameZh());
    }

    @Test
    @DisplayName("AI returns invalid JSON - throws AIServiceException")
    void identifyFood_invalidJson() {
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse("{invalid json}"))
                .addHeader("Content-Type", "application/json"));

        assertThrows(AIServiceException.class, () -> geminiService.identifyFoodFromText("some food"));
    }

    @Test
    @DisplayName("AI returns empty food list")
    void identifyFood_emptyList() {
        String emptyResponse = "{\"foods\":[], \"meal_description\":\"\", \"confidence\": 0.1}";
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse(emptyResponse))
                .addHeader("Content-Type", "application/json"));

        FoodIdentificationResult result = geminiService.identifyFoodFromText("a picture of my desk");
        assertNotNull(result);
        assertTrue(result.getFoods().isEmpty());
        assertTrue(result.getConfidence() < 0.5);
    }

    @Test
    @DisplayName("API timeout - throws exception")
    void identifyFood_timeout() {
        mockServer.enqueue(new MockResponse()
                .setBodyDelay(10, TimeUnit.SECONDS)
                .setBody("{}"));

        assertThrows(AIServiceException.class, () -> geminiService.identifyFoodFromText("some food"));
    }

    @Test
    @DisplayName("Gemini response with markdown code fences - strips them correctly")
    void parseFoodResult_withMarkdownFences() {
        String json = """
                ```json
                {"foods":[], "meal_description":"test", "confidence": 0.5}
                ```""";

        FoodIdentificationResult result = geminiService.parseFoodIdentificationResult(json);
        assertNotNull(result);
        assertEquals("test", result.getMealDescription());
    }

    @Test
    @DisplayName("Intent classification - FOOD_LOG")
    void classifyIntent_foodLog() {
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse("FOOD_LOG"))
                .addHeader("Content-Type", "application/json"));

        UserIntent intent = geminiService.classifyIntent("I had chicken and rice for lunch");
        assertEquals(UserIntent.FOOD_LOG, intent);
    }

    @Test
    @DisplayName("Intent classification - DIET_ADVICE")
    void classifyIntent_dietAdvice() {
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse("DIET_ADVICE"))
                .addHeader("Content-Type", "application/json"));

        UserIntent intent = geminiService.classifyIntent("How should I eat to lose weight?");
        assertEquals(UserIntent.DIET_ADVICE, intent);
    }

    @Test
    @DisplayName("Intent classification - unrecognized defaults to GENERAL_CHAT")
    void classifyIntent_fallback() {
        mockServer.enqueue(new MockResponse()
                .setBody(wrapJsonInGeminiResponse("SOMETHING_RANDOM"))
                .addHeader("Content-Type", "application/json"));

        UserIntent intent = geminiService.classifyIntent("hello");
        assertEquals(UserIntent.GENERAL_CHAT, intent);
    }

    private String loadTestResource(String filename) throws IOException {
        return new String(
                getClass().getClassLoader().getResourceAsStream("testdata/" + filename).readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
