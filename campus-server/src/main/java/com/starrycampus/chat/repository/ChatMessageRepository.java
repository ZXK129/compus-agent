package com.starrycampus.chat.repository;

import com.starrycampus.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByStudentIdOrderByCreatedAtAsc(Long studentId);
    void deleteByStudentId(Long studentId);

    /**
     * 删除指定时间之前的所有聊天记录（用于定时清理过期数据）
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE m.createdAt < :cutoff")
    int deleteByCreatedAtBefore(LocalDateTime cutoff);
}
