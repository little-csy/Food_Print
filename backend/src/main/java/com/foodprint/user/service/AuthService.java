package com.foodprint.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class AuthService {

    private final WebClient webClient;
    private final String serviceRoleKey;

    public AuthService(
            WebClient.Builder webClientBuilder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this.webClient = webClientBuilder.baseUrl(supabaseUrl).build();
        this.serviceRoleKey = serviceRoleKey;
    }

    /**
     * Triggers a password reset email via Supabase Auth Admin API.
     * Always completes without throwing (caller returns 200 regardless to prevent email enumeration).
     */
    public void sendPasswordResetEmail(String email) {
        webClient.post()
                .uri("/auth/v1/recover")
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", email))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        "Supabase password reset failed [" + response.statusCode() + "]: " + body)))
                .toBodilessEntity()
                .block();
    }
}
