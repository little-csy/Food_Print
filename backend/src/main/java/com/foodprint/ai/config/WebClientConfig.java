package com.foodprint.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient(AIConfig aiConfig) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(aiConfig.getGemini().getTimeoutSeconds()));

        // Allow larger response sizes for AI responses
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();

        return WebClient.builder()
                .baseUrl(aiConfig.getGemini().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean(name = "usdaWebClient")
    public WebClient usdaWebClient(AIConfig aiConfig) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(aiConfig.getUsda().getTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(aiConfig.getUsda().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
