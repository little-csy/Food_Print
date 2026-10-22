package com.foodprint.ai.controller;

import com.foodprint.ai.dto.AIResponse;
import com.foodprint.ai.dto.SaveAIAnalysisLogRequest;
import com.foodprint.ai.dto.UserNutritionContext;
import com.foodprint.ai.exception.AIServiceException;
import com.foodprint.ai.service.AIMealLogService;
import com.foodprint.ai.service.AIRequestRouter;
import com.foodprint.food.entity.FoodLog;
import com.foodprint.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for AI food analysis and diet advice endpoints.
 * All responses wrapped in unified ApiResponse format.
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    private final AIRequestRouter router;
    private final AIMealLogService aiMealLogService;

    public AIController(AIRequestRouter router, AIMealLogService aiMealLogService) {
        this.router = router;
        this.aiMealLogService = aiMealLogService;
    }

    /**
     * POST /api/ai/log
     * Save AI analysis result as a meal log.
     */
    @PostMapping("/log")
    public ResponseEntity<ApiResponse<FoodLog>> logAnalysis(
            @RequestBody SaveAIAnalysisLogRequest request,
            @RequestHeader("userId") UUID userId) {
        log.info("Saving AI analysis log for user: {}", userId);
        FoodLog savedLog = aiMealLogService.saveAnalysisLog(userId, request);
        return ResponseEntity.ok(ApiResponse.success(savedLog));
    }

    /**
     * POST /api/ai/analyze/image
     * Upload a food image for AI analysis (multipart binary).
     */
    @PostMapping(value = "/analyze/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AIResponse>> analyzeImage(
            @RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Image file is empty"));
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Unsupported file type. Please upload an image (JPEG, PNG, WebP)."));
        }

        try {
            byte[] imageData = image.getBytes();
            AIResponse response = router.routeImage(imageData, contentType);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IOException e) {
            log.error("Failed to read uploaded image", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Failed to read uploaded image"));
        }
    }

    /**
     * POST /api/ai/analyze
     * Unified endpoint for text input and/or imageUrl.
     * Auto-classifies intent and routes to correct pipeline.
     *
     * Body examples:
     * { "text": "I had chicken for lunch" }
     * { "text": "I had chicken for lunch", "userContext": { ... } }
     * { "imageUrl": "https://xxx.supabase.co/storage/v1/food.jpg" }
     * { "imageUrl": "...", "text": "this is my lunch" }
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<AIResponse>> analyze(@RequestBody AnalyzeRequest request) {
        // imageUrl takes priority (food analysis from image)
        if (request.imageUrl != null && !request.imageUrl.isBlank()) {
            log.info("Analyzing from imageUrl: {}", request.imageUrl);
            AIResponse response = router.routeImageUrl(request.imageUrl);
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        // Text input
        if (request.text != null && !request.text.isBlank()) {
            AIResponse response = router.routeText(request.text, request.userContext);
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "Please provide 'text' or 'imageUrl'"));
    }

    /**
     * Global exception handler for AI errors.
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleAIError(AIServiceException e) {
        log.error("AI service error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.error(500, e.getMessage()));
    }

    /**
     * Request DTO — supports text, imageUrl, or both.
     */
    public static class AnalyzeRequest {
        public String text;
        public String imageUrl;
        public UserNutritionContext userContext;
    }
}
