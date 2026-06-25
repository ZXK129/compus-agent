package com.starrycampus.chat.controller;

import com.starrycampus.chat.dto.ChatMessageDTO;
import com.starrycampus.chat.dto.ChatRequest;
import com.starrycampus.chat.dto.ChatResponse;
import com.starrycampus.chat.service.AiChatService;
import com.starrycampus.common.base.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class AiChatController {

    private final AiChatService aiChatService;
    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public AiChatController(AiChatService aiChatService) { this.aiChatService = aiChatService; }

    @PostMapping
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        String reply = aiChatService.chat(defaultStudentId, request.getMessage());
        List<ChatMessageDTO> messages = aiChatService.getHistory(defaultStudentId);
        return ApiResponse.success(ChatResponse.builder().text(reply).messages(messages).build());
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatMessageDTO>> getHistory() {
        return ApiResponse.success(aiChatService.getHistory(defaultStudentId));
    }

    @DeleteMapping("/history")
    public ApiResponse<Void> clearHistory() {
        aiChatService.clearHistory(defaultStudentId);
        return ApiResponse.success("聊天记录已清空", null);
    }
}
