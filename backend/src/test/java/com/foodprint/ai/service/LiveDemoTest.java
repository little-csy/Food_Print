package com.foodprint.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.dto.*;
import com.foodprint.ai.dto.FoodAnalysisResult.FoodNutritionDetail;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LIVE DEMO TEST — calls real Gemini API.
 * Run with: mvn test -Dtest="LiveDemoTest" -Dfoodprint.live=true
 * 
 * Demonstrates intent routing + calorie analysis with real AI.
 * Adds delays between calls to avoid 429 rate limiting.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LiveDemoTest {

    private static GeminiService geminiService;
    private static FoodAnalysisService foodAnalysisService;
    private static AIRequestRouter router;

    private static final boolean LIVE_ENABLED = "true".equals(System.getProperty("foodprint.live", "false"));

    @BeforeAll
    static void setUp() {
        if (!LIVE_ENABLED)
            return;

        AIConfig config = new AIConfig();
        AIConfig.GeminiConfig geminiConfig = new AIConfig.GeminiConfig();
        // TODO: Set your Gemini API key here or via env: GEMINI_API_KEY
        geminiConfig.setApiKey(System.getenv().getOrDefault("GEMINI_API_KEY", "YOUR_GEMINI_API_KEY"));
        geminiConfig.setModel("gemini-2.5-flash");
        geminiConfig.setBaseUrl("https://generativelanguage.googleapis.com/v1beta");
        geminiConfig.setTimeoutSeconds(60);
        config.setGemini(geminiConfig);

        AIConfig.USDAConfig usdaConfig = new AIConfig.USDAConfig();
        usdaConfig.setApiKey("DEMO_KEY");
        usdaConfig.setBaseUrl("https://api.nal.usda.gov/fdc/v1");
        usdaConfig.setTimeoutSeconds(10);
        config.setUsda(usdaConfig);

        WebClient geminiWebClient = WebClient.builder()
                .baseUrl(geminiConfig.getBaseUrl())
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
        WebClient usdaWebClient = WebClient.builder()
                .baseUrl(usdaConfig.getBaseUrl())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        geminiService = new GeminiService(geminiWebClient, config, objectMapper);
        NutritionLookupService nutritionLookup = new NutritionLookupService(usdaWebClient, config, objectMapper);
        NutritionPostProcessor postProcessor = new NutritionPostProcessor();
        foodAnalysisService = new FoodAnalysisService(geminiService, nutritionLookup, postProcessor);
        DietAdviceService dietAdviceService = new DietAdviceService(geminiService);

        com.foodprint.ai.repository.ChatMessageRepository mockRepo = org.mockito.Mockito
                .mock(com.foodprint.ai.repository.ChatMessageRepository.class);
        ChatService chatService = new ChatService(mockRepo, geminiWebClient, config, objectMapper);

        router = new AIRequestRouter(geminiService, foodAnalysisService, dietAdviceService, chatService);
    }

    private void rateLimitDelay() throws InterruptedException {
        Thread.sleep(6000); // 6s between calls to stay well under 15 req/min
    }

    // ─────────────────────────────────────────────
    // Demo 1: Intent Router — 3 different intents in one test
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("🎯 Demo 1: Intent Router — classifies 3 intents correctly")
    void demo_intentRouter() throws Exception {
        if (!LIVE_ENABLED)
            return;
        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 1: Intent Classification");
        System.out.println("=".repeat(60));

        // Test 1: FOOD_LOG
        String foodInput = "For lunch I had grilled chicken breast with rice and broccoli";
        System.out.println("\nInput: \"" + foodInput + "\"");
        UserIntent intent1 = geminiService.classifyIntent(foodInput);
        System.out.println("  -> Classified as: " + intent1);
        assertEquals(UserIntent.FOOD_LOG, intent1);

        rateLimitDelay();

        // Test 2: DIET_ADVICE
        String adviceInput = "I want to lose weight, what should I eat?";
        System.out.println("\nInput: \"" + adviceInput + "\"");
        UserIntent intent2 = geminiService.classifyIntent(adviceInput);
        System.out.println("  -> Classified as: " + intent2);
        assertEquals(UserIntent.DIET_ADVICE, intent2);

        rateLimitDelay();

        // Test 3: GENERAL_CHAT
        String chatInput = "I'm so tired today, don't feel like cooking";
        System.out.println("\nInput: \"" + chatInput + "\"");
        UserIntent intent3 = geminiService.classifyIntent(chatInput);
        System.out.println("  -> Classified as: " + intent3);
        assertEquals(UserIntent.GENERAL_CHAT, intent3);

        System.out.println("\n>>> All 3 intents classified correctly!");
    }

    // ─────────────────────────────────────────────
    // Demo 2: Text → Calorie Analysis
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("🍽 Demo 2: Text food analysis + calories")
    void demo_textCalorieAnalysis() throws Exception {
        if (!LIVE_ENABLED)
            return;

        rateLimitDelay();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 2: Text -> Food Analysis + Calorie Estimation");
        System.out.println("=".repeat(60));

        String text = Files.readString(
                Path.of("src/test/resources/testdata/demo_text1.txt")).trim();
        System.out.println("Input: \"" + text + "\"");
        System.out.println("-".repeat(60));

        FoodAnalysisResult result = foodAnalysisService.analyzeFromText(text);

        System.out.println("AI Confidence: " + result.getConfidence());
        System.out.println("Foods identified: " + result.getFoods().size());
        System.out.println();
        System.out.printf("  %-25s | %8s | %8s %8s %8s | %s%n",
                "FOOD", "CALORIES", "PROTEIN", "FAT", "CARBS", "SOURCE");
        System.out.println("  " + "-".repeat(85));

        for (FoodNutritionDetail food : result.getFoods()) {
            System.out.printf("  %-25s | %5.0f kcal | %5.1f g  %5.1f g  %5.1f g  | %s%s%n",
                    food.getNameEn(),
                    food.getCalories(),
                    food.getProteinG(), food.getFatG(), food.getCarbsG(),
                    food.getDataSource(),
                    food.isFlagged() ? " !! " + food.getFlagReason() : "");
        }

        System.out.println("  " + "-".repeat(85));
        System.out.printf("  %-25s | %5.0f kcal | %5.1f g  %5.1f g  %5.1f g  | %d items%n",
                "MEAL TOTAL",
                result.getSummary().getTotalCalories(),
                result.getSummary().getTotalProteinG(),
                result.getSummary().getTotalFatG(),
                result.getSummary().getTotalCarbsG(),
                result.getSummary().getFoodCount());

        assertTrue(result.getFoods().size() >= 3, "Should identify at least 3 foods");
        assertTrue(result.getSummary().getTotalCalories() > 300, "Total calories should be > 300");
    }

    // ─────────────────────────────────────────────
    // Demo 3: Image → Calorie Analysis
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("📷 Demo 3: Image food analysis + calories")
    void demo_imageCalorieAnalysis() throws Exception {
        if (!LIVE_ENABLED)
            return;

        rateLimitDelay();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 3: Image -> Food Analysis + Calorie Estimation");
        System.out.println("=".repeat(60));

        byte[] imageData = Files.readAllBytes(
                Path.of("src/test/resources/testdata/food2.jpg"));
        System.out.println("Input: food2.jpg (steak plate, " + imageData.length / 1024 + " KB)");
        System.out.println("-".repeat(60));

        FoodAnalysisResult result = foodAnalysisService.analyzeFromImage(imageData, "image/jpeg");

        System.out.println("AI Confidence: " + result.getConfidence());
        System.out.println("Foods identified: " + result.getFoods().size());
        System.out.println();
        System.out.printf("  %-25s | %8s | %8s %8s %8s | %s%n",
                "FOOD", "CALORIES", "PROTEIN", "FAT", "CARBS", "SOURCE");
        System.out.println("  " + "-".repeat(85));

        for (FoodNutritionDetail food : result.getFoods()) {
            System.out.printf("  %-25s | %5.0f kcal | %5.1f g  %5.1f g  %5.1f g  | %s%s%n",
                    food.getNameEn(),
                    food.getCalories(),
                    food.getProteinG(), food.getFatG(), food.getCarbsG(),
                    food.getDataSource(),
                    food.isFlagged() ? " !! " + food.getFlagReason() : "");
        }

        System.out.println("  " + "-".repeat(85));
        System.out.printf("  %-25s | %5.0f kcal | %5.1f g  %5.1f g  %5.1f g  | %d items%n",
                "MEAL TOTAL",
                result.getSummary().getTotalCalories(),
                result.getSummary().getTotalProteinG(),
                result.getSummary().getTotalFatG(),
                result.getSummary().getTotalCarbsG(),
                result.getSummary().getFoodCount());

        assertTrue(result.getFoods().size() >= 1);
    }

    // ─────────────────────────────────────────────
    // Demo 4: Dietary Preference check (Vegan)
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("🌱 Demo 4: Diet Advice with Vegan Preference")
    void demo_dietAdviceWithPreference() throws Exception {
        if (!LIVE_ENABLED)
            return;
        rateLimitDelay();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("DEMO 4: Diet Advice respecting Vegan Preference");
        System.out.println("=".repeat(60));

        UserNutritionContext ctx = new UserNutritionContext();
        ctx.setHeightCm(175.0);
        ctx.setWeightKg(80.0);
        ctx.setAge(28);
        ctx.setGender("Male");
        ctx.setGoal("LOSE_WEIGHT");
        ctx.setDailyCalorieTarget(2200);

        ctx.setDietaryPreference("Vegan"); // KEY SETTING

        ctx.setTodayCalories(800.0);
        ctx.setTodayProtein(20.0);
        ctx.setTodayFat(30.0);
        ctx.setTodayCarbs(110.0);

        String advice = geminiService.generateDietAdvice(ctx);

        System.out.println("AI Advice (Notice if it suggests plant-based proteins instead of meat/dairy):\n");
        System.out.println(advice);
        System.out.println("\n" + "-".repeat(60));

        assertNotNull(advice);
        assertFalse(advice.isBlank());
    }
}
