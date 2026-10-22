package com.foodprint.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodprint.ai.config.AIConfig;
import com.foodprint.ai.entity.ChatMessage;
import com.foodprint.ai.repository.ChatMessageRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    private MockWebServer mockWebServer;
    private WebClient mockWebClient;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private AIConfig config;

    private ChatService chatService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockWebClient = WebClient.create(mockWebServer.url("/").toString());

        AIConfig.GeminiConfig geminiConfig = new AIConfig.GeminiConfig();
        geminiConfig.setApiKey("test-key");
        geminiConfig.setModel("test-model");
        when(config.getGemini()).thenReturn(geminiConfig);

        chatService = new ChatService(chatMessageRepository, mockWebClient, config, objectMapper);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("chat returns AI response and saves messages to repository")
    void testChat() {
        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"Hi there!\"}]}}]}";
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        UUID userId = UUID.randomUUID();
        when(chatMessageRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(new ChatMessage(userId, "USER", "Tell me a joke")));

        String response = chatService.chat("How are you?", userId.toString());

        assertEquals("Hi there!", response);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("chat without userId still calls Gemini but does not save to history")
    void testChatNoHistory() {
        String mockResponse = "{\"candidates\": [{\"content\": {\"parts\": [{\"text\": \"I'm a bot!\"}]}}]}";
        mockWebServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        String response = chatService.chat("Who are you?", null);

        assertEquals("I'm a bot!", response);
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }
}
