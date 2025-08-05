package com.soprasteria.smartjobportal.service;

import com.soprasteria.smartjobportal.config.GeminiConfig;
import com.soprasteria.smartjobportal.dto.ChatbotDTO.ChatRequest;
import com.soprasteria.smartjobportal.dto.ChatbotDTO.ChatResponse;
import com.soprasteria.smartjobportal.dto.ChatbotDTO.GeminiRequest;
import com.soprasteria.smartjobportal.dto.ChatbotDTO.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final RestTemplate restTemplate;
    private final GeminiConfig geminiConfig;

    /**
     * Process a user's chat message and get a response from Gemini AI
     * @param chatRequest The user's chat request
     * @return A response from the AI
     */
    public ChatResponse processChat(ChatRequest chatRequest) {
        // Check if API key is configured
        if (geminiConfig.getApiKey() == null || geminiConfig.getApiKey().isEmpty()) {
            log.error("Gemini API key is not configured");
            return ChatResponse.builder()
                    .response("The chatbot is currently unavailable. Please contact the administrator to set up the API key.")
                    .build();
        }
        
        try {
            // Create enhanced prompt
            String enhancedPrompt = enhancePrompt(chatRequest.getMessage());
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Build URL with API key
            String url = UriComponentsBuilder.fromHttpUrl(geminiConfig.getApiUrl())
                        .queryParam("key", geminiConfig.getApiKey())
                        .toUriString();
            
            log.debug("Calling API at URL: {}", geminiConfig.getApiUrl());
            
            // Create request for Gemini API
            Map<String, Object> requestMap = new HashMap<>();
            
            // Create the contents object for Gemini 1.5 Flash
            Map<String, Object> contents = new HashMap<>();
            
            // Add role for better context (optional for Gemini models)
            contents.put("role", "user");
            
            // Add parts with the message
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", enhancedPrompt);
            parts.add(part);
            contents.put("parts", parts);
            
            // Add to request as an array with a single content object
            requestMap.put("contents", List.of(contents));
            
            // Add generation config with recommended parameters for Gemini 1.5 Flash
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestMap.put("generationConfig", generationConfig);
            
            // Make API call
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestMap, headers);
            Map<String, Object> responseMap = restTemplate.postForObject(url, requestEntity, Map.class);
            
            // Extract response from Gemini format
            String responseText = "Sorry, I couldn't process your request.";
            
            if (responseMap != null && responseMap.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                if (!candidates.isEmpty() && candidates.get(0).containsKey("content")) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content.containsKey("parts")) {
                        List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
                        if (!responseParts.isEmpty() && responseParts.get(0).containsKey("text")) {
                            responseText = (String) responseParts.get(0).get("text");
                        }
                    }
                }
            }
            
            return ChatResponse.builder()
                    .response(responseText)
                    .build();
        } catch (Exception e) {
            // Log detailed error information
            log.error("Error processing chat request: {} - {}", e.getClass().getName(), e.getMessage(), e);
            
            // Create a more informative error message for debugging
            String errorDetails = "API Error: " + e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorDetails += " - " + e.getMessage();
            }
            
            log.info("Error details: {}", errorDetails);
            
            // Provide a more helpful message based on the error
            String userMessage;
            if (e.getMessage() != null && e.getMessage().contains("NOT_FOUND")) {
                userMessage = "The chatbot is currently experiencing issues with the AI model. " +
                              "Please check your API configuration or contact the administrator. " +
                              "Error details: " + errorDetails;
            } else if (e.getMessage() != null && e.getMessage().contains("PERMISSION_DENIED")) {
                userMessage = "The chatbot doesn't have permission to access the AI model. " +
                              "This might be due to API key restrictions or quota limits. " +
                              "Please contact the administrator.";
            } else {
                userMessage = "Sorry, I encountered an error while processing your request. " + 
                             "Please try again later. Error details: " + errorDetails;
            }
            
            return ChatResponse.builder()
                    .response(userMessage)
                    .build();
        }
    }
    
    /**
     * Enhance the user prompt with job-related context
     * @param userMessage The original user message
     * @return Enhanced prompt for better job-related responses
     */
    private String enhancePrompt(String userMessage) {
        return "You are a helpful job search assistant for a Smart Job Portal. " +
               "Please provide helpful, accurate, and concise information about job searching, " +
               "interview preparation, resume building, or career advice. " +
               "If the question is not related to jobs or careers, politely explain that you can only " +
               "answer job-related questions. Here's the user's question: " + userMessage;
    }
}
