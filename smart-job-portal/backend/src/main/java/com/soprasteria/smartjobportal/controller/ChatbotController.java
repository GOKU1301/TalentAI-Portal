package com.soprasteria.smartjobportal.controller;

import com.soprasteria.smartjobportal.dto.ChatbotDTO.ChatRequest;
import com.soprasteria.smartjobportal.dto.ChatbotDTO.ChatResponse;
import com.soprasteria.smartjobportal.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    /**
     * Endpoint to process chatbot messages
     * @param chatRequest The user's chat message
     * @return AI-generated response
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest) {
        ChatResponse response = chatbotService.processChat(chatRequest);
        return ResponseEntity.ok(response);
    }
}
