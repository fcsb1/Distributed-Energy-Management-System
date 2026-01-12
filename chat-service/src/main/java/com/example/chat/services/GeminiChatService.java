package com.example.chat.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiChatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiChatService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiChatService(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public String getResponse(String userMessage) {
        if (apiKey == null || apiKey.isEmpty()) {
            LOGGER.warn("Gemini API key not configured");
            return "I'm sorry, the AI service is not configured. Please contact an administrator.";
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(Map.of("text", userMessage)));
            requestBody.put("contents", List.of(content));

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseGeminiResponse(response.body());
            } else {
                LOGGER.error("Gemini API error: {}", response.body());
                return "I'm having trouble connecting to the AI service. Please try again later."; }




        } catch (Exception e) {
            LOGGER.error("Error calling Gemini API", e);
            return "I'm experiencing technical difficulties. Please try again later or contact an administrator.";
        }
    }

    private String parseGeminiResponse(String responseBody) {
        try {
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }

            return "I couldn't generate a response. Please try rephrasing your question.";
        } catch (Exception e) {
            LOGGER.error("Error parsing Gemini response", e);
            return "I couldn't process the response. Please try again.";
        }
    }
}