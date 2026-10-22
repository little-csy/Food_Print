package com.foodprint.ai.service;

import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.FoodAnalysisResult;
import com.foodprint.ai.dto.UserNutritionContext;
import com.foodprint.ai.exception.AIServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

/**
 * Routes AI requests to the appropriate pipeline based on input type and
 * intent.
 *
 * - Image binary → food analysis
 * - Image URL → download then food analysis
 * - Text input → classify intent → FOOD_LOG / DIET_ADVICE / DIET_PLAN /
 * DIET_ROAST /
 * GENERAL_CHAT
 */
@Service
public class AIRequestRouter {

    private static final Logger log = LoggerFactory.getLogger(AIRequestRouter.class);

    private final GeminiService geminiService;
    private final FoodAnalysisService foodAnalysisService;
    private final DietAdviceService dietAdviceService;
    private final ChatService chatService;
    private final WebClient downloadClient;

    public AIRequestRouter(GeminiService geminiService,
            FoodAnalysisService foodAnalysisService,
            DietAdviceService dietAdviceService,
            ChatService chatService) {
        this.geminiService = geminiService;
        this.foodAnalysisService = foodAnalysisService;
        this.dietAdviceService = dietAdviceService;
        this.chatService = chatService;
        this.downloadClient = WebClient.create();
    }

    /**
     * Route an image binary input — always goes to food analysis.
     */
    public AIResponse routeImage(byte[] imageData, String mimeType) {
        log.info("Routing image input to food analysis");
        FoodAnalysisResult result = foodAnalysisService.analyzeFromImage(imageData, mimeType);
        return AIResponse.foodAnalysis(result);
    }

    /**
     * Route an image URL — download from Supabase Storage (or any URL),
     * then analyze. Used by backend service (Engineer B) who passes imageUrl.
     */
    public AIResponse routeImageUrl(String imageUrl) {
        log.info("Downloading image from URL: {}", imageUrl);
        try {
            byte[] imageData = downloadClient.get()
                    .uri(URI.create(imageUrl))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (imageData == null || imageData.length == 0) {
                throw new AIServiceException("Downloaded image is empty", "IMAGE_DOWNLOAD_ERROR");
            }

            // Detect mime type from URL
            String mimeType = detectMimeType(imageUrl);
            log.info("Downloaded {} bytes, mimeType={}", imageData.length, mimeType);

            FoodAnalysisResult result = foodAnalysisService.analyzeFromImage(imageData, mimeType);
            return AIResponse.foodAnalysis(result);
        } catch (AIServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download image from URL: {}", imageUrl, e);
            throw new AIServiceException("Failed to download image: " + e.getMessage(), "IMAGE_DOWNLOAD_ERROR", e);
        }
    }

    /**
     * Route a text input — classify intent first, then dispatch.
     */
    public AIResponse routeText(String text, UserNutritionContext userContext) {
        UserIntent intent = geminiService.classifyIntent(text);
        log.info("Classified intent: {} for text: '{}'", intent,
                text.length() > 50 ? text.substring(0, 50) + "..." : text);

        return switch (intent) {
            case FOOD_LOG -> {
                FoodAnalysisResult result = foodAnalysisService.analyzeFromText(text);
                yield AIResponse.foodAnalysis(result);
            }
            case DIET_ADVICE -> dietAdviceService.generateAdvice(userContext);
            case DIET_PLAN -> dietAdviceService.generatePlan(userContext);
            case DIET_ROAST -> dietAdviceService.generateRoast(userContext);
            case GENERAL_CHAT -> {
                String userId = (userContext != null) ? userContext.getUserId() : null;
                String chatResponse = chatService.chat(text, userId);
                yield AIResponse.chat(chatResponse);
            }
        };
    }

    private String detectMimeType(String url) {
        String lower = url.toLowerCase();
        if (lower.contains(".png"))
            return "image/png";
        if (lower.contains(".webp"))
            return "image/webp";
        if (lower.contains(".gif"))
            return "image/gif";
        return "image/jpeg"; // default
    }
}
