package com.foodprint.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.entity.ChatMessage;
import com.foodprint.ai.exception.AIServiceException;
import com.foodprint.ai.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final WebClient webClient;
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;

    public ChatService(ChatMessageRepository chatMessageRepository,
            @Qualifier("geminiWebClient") WebClient webClient,
            AIConfig aiConfig,
            ObjectMapper objectMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.webClient = webClient;
        this.aiConfig = aiConfig;
        this.objectMapper = objectMapper;
    }

    public String chat(String message, String userIdStr) {
        UUID userId = null;
        try {
            if (userIdStr != null && !userIdStr.isBlank()) {
                userId = UUID.fromString(userIdStr);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", userIdStr);
            userId = null;
        }

        // 1. Fetch history if userId is present
        List<Map<String, Object>> contents = new ArrayList<>();

        // System prompt
        Map<String, Object> systemContent = Map.of(
                "role", "model",
                "parts", List.of(Map.of("text",
                        "You are a friendly health and nutrition assistant for the FoodPrint app. Keep responses helpful, concise, and related to food and health.")));
        contents.add(systemContent);

        if (userId != null) {
            List<ChatMessage> history = chatMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
            Collections.reverse(history); // Oldest to newest for prompt

            for (ChatMessage msg : history) {
                contents.add(Map.of(
                        "role", msg.getRole().equals("USER") ? "user" : "model",
                        "parts", List.of(Map.of("text", msg.getContent()))));
            }

            // Save user message
            ChatMessage userMsgEntity = new ChatMessage(userId, "USER", message);
            chatMessageRepository.save(userMsgEntity);
        }

        // Current message
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", message))));

        // 2. Call Gemini API
        Map<String, Object> requestBody = Map.of("contents", contents);
        String aiResponseText = callGemini(requestBody);

        // 3. Save AI response
        if (userId != null) {
            ChatMessage aiMsgEntity = new ChatMessage(userId, "AI", aiResponseText);
            chatMessageRepository.save(aiMsgEntity);
        }

        return aiResponseText;
    }

    private String callGemini(Map<String, Object> requestBody) {
        String model = aiConfig.getGemini().getModel();
        String apiKey = aiConfig.getGemini().getApiKey();

        try {
            String response = webClient.post()
                    .uri("/models/{model}:generateContent?key={key}", model, apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractTextFromResponse(response);
        } catch (Exception e) {
            log.error("Gemini API call failed", e);
            throw new AIServiceException("Gemini call failed for chat: " + e.getMessage(), "GEMINI_ERROR", e);
        }
    }

    private String extractTextFromResponse(String responseJson) {
        try {
            var root = objectMapper.readTree(responseJson);
            var candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                throw new AIServiceException("Gemini returned empty candidates", "GEMINI_EMPTY_RESPONSE");
            }
            return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (JsonProcessingException e) {
            throw new AIServiceException("Failed to parse Gemini response for chat", "GEMINI_PARSE_ERROR", e);
        }
    }
}
