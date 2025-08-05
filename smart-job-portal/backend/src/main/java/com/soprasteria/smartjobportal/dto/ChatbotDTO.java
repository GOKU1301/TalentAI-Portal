package com.soprasteria.smartjobportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatbotDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResponse {
        private String response;
    }

    // API request/response DTOs
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeminiRequest {
        // For Gemini models
        private Contents contents;
        
        // For Gemini models
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Contents {
            private Part[] parts;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Part {
                private String text;
            }
        }
        
        public static GeminiRequest fromUserMessage(String message) {
            GeminiRequest request = new GeminiRequest();
            
            // Format for Gemini models
            Contents.Part part = new Contents.Part(message);
            Contents.Part[] parts = new Contents.Part[]{part};
            Contents contents = new Contents(parts);
            request.setContents(contents);
            
            return request;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeminiResponse {
        // For Gemini models
        private Candidate[] candidates;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Candidate {
            private Content content;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Content {
                private Part[] parts;
                
                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                public static class Part {
                    private String text;
                }
            }
        }
        
        public String getResponseText() {
            // Check for Gemini response format
            if (candidates != null && candidates.length > 0 && 
                candidates[0].content != null && 
                candidates[0].content.parts != null && 
                candidates[0].content.parts.length > 0) {
                return candidates[0].content.parts[0].text;
            }
            
            return "Sorry, I couldn't process your request.";
        }
    }
}
