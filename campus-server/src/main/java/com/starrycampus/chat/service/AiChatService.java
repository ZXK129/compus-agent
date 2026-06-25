package com.starrycampus.chat.service;

import com.starrycampus.chat.dto.ChatMessageDTO;
import java.util.List;

public interface AiChatService {
    String chat(Long studentId, String message);
    List<ChatMessageDTO> getHistory(Long studentId);
    void clearHistory(Long studentId);
}
