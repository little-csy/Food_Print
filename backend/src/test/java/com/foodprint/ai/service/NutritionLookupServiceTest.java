package com.foodprint.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.dto.FoodIdentificationResult.FoodItem;
import com.foodprint.ai.dto.NutritionData;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Isolated tests for NutritionLookupService.
 * Uses MockWebServer — no real USDA API calls.
 */
class NutritionLookupServiceTest {

    private MockWebServer mockServer;
    private NutritionLookupService service;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        AIConfig config = new AIConfig();
        AIConfig.USDAConfig usdaConfig = new AIConfig.USDAConfig();
        usdaConfig.setApiKey("test-key");
        usdaConfig.setBaseUrl(mockServer.url("/fdc/v1").toString());
        usdaConfig.setTimeoutSeconds(5);
        config.setUsda(usdaConfig);

        WebClient webClient = WebClient.builder()
                .baseUrl(mockServer.url("/fdc/v1").toString())
                .build();

        service = new NutritionLookupService(webClient, config, new ObjectMapper());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    private FoodItem createFoodItem(String name, double portion, String unit,
            double cal, double protein, double fat, double carbs, double fiber) {
        FoodItem item = new FoodItem();
        item.setNameEn(name);
        item.setPortionAmount(portion);
        item.setPortionUnit(unit);
        item.setEstimatedCalories(cal);
        item.setEstimatedProteinG(protein);
        item.setEstimatedFatG(fat);
        item.setEstimatedCarbsG(carbs);
        item.setEstimatedFiberG(fiber);
        return item;
    }

    @Test
    @DisplayName("USDA found - returns accurate nutrition data with USDA source")
    void lookup_usdaFound() throws Exception {
        String usdaResponse = loadTestResource("usda_response_chicken.json");
        mockServer.enqueue(new MockResponse()
                .setBody(usdaResponse)
                .addHeader("Content-Type", "application/json"));

        FoodItem chicken = createFoodItem("grilled chicken breast", 150, "g",
                198, 37, 4.3, 0, 0);

        NutritionData result = service.lookup(chicken);

        assertEquals("USDA", result.getSource());
        // USDA: 165 kcal per 100g, for 150g = 247.5
        assertEquals(247.5, result.getCalories(), 0.1);
        // USDA: 31g protein per 100g, for 150g = 46.5
        assertEquals(46.5, result.getProteinG(), 0.1);
    }

    @Test
    @DisplayName("USDA empty result - falls back to AI estimate")
    void lookup_usdaEmpty_fallbackToAI() throws Exception {
        String emptyResponse = loadTestResource("usda_response_empty.json");
        mockServer.enqueue(new MockResponse()
                .setBody(emptyResponse)
                .addHeader("Content-Type", "application/json"));

        FoodItem unknownFood = createFoodItem("mystery food xyz", 100, "g",
                200, 10, 8, 20, 2);

        NutritionData result = service.lookup(unknownFood);

        assertEquals("AI_ESTIMATE", result.getSource());
        assertEquals(200, result.getCalories());
        assertEquals(10, result.getProteinG());
    }

    @Test
    @DisplayName("USDA API error 500 - falls back to AI estimate without crashing")
    void lookup_usdaError_fallbackToAI() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        FoodItem food = createFoodItem("chicken breast", 100, "g",
                165, 31, 3.6, 0, 0);

        NutritionData result = service.lookup(food);

        assertEquals("AI_ESTIMATE", result.getSource());
        assertEquals(165, result.getCalories());
    }

    @Test
    @DisplayName("Portion conversion - g, oz, cup, tbsp")
    void convertToGrams_variousUnits() {
        assertEquals(100, NutritionLookupService.convertToGrams(100, "g"));
        assertEquals(28.35, NutritionLookupService.convertToGrams(1, "oz"), 0.01);
        assertEquals(240, NutritionLookupService.convertToGrams(1, "cup"));
        assertEquals(15, NutritionLookupService.convertToGrams(1, "tbsp"));
        assertEquals(5, NutritionLookupService.convertToGrams(1, "tsp"));
        assertEquals(453.6, NutritionLookupService.convertToGrams(1, "lb"), 0.1);
    }

    @Test
    @DisplayName("Portion conversion - unknown unit defaults to raw amount")
    void convertToGrams_unknownUnit() {
        assertEquals(42, NutritionLookupService.convertToGrams(42, "unknown_unit"));
    }

    private String loadTestResource(String filename) throws IOException {
        return new String(
                getClass().getClassLoader().getResourceAsStream("testdata/" + filename).readAllBytes(),
                StandardCharsets.UTF_8);
    }
}
