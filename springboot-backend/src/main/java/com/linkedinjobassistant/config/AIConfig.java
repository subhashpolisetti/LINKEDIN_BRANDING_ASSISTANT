package com.linkedinjobassistant.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class AIConfig {

    @Value("${app.ai.openai-api-key}")
    private String openAiApiKey;

    @Value("${app.ai.google-api-key}")
    private String googleApiKey;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAiApiKey, Duration.ofSeconds(60));
    }

    // Method to get Google API key for Gemini AI
    public String getGoogleApiKey() {
        return googleApiKey;
    }

    // Note: Google's Gemini AI Java SDK is not yet available in Maven Central
    // We'll need to make direct HTTP calls to the API or use their REST API
    // Once the official SDK is available, we can add proper configuration here
}
