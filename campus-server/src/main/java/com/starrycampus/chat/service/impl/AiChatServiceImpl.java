package com.starrycampus.chat.service.impl;

import com.starrycampus.card.service.CardService;
import com.starrycampus.chat.dto.ChatMessageDTO;
import com.starrycampus.chat.entity.ChatMessage;
import com.starrycampus.chat.repository.ChatMessageRepository;
import com.starrycampus.chat.service.AiChatService;
import com.starrycampus.library.service.LibraryService;
import com.starrycampus.presence.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 聊天服务实现 — Spring AI ChatClient + Function Calling
 * 框架自动处理工具调用的完整生命周期
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private final ChatMessageRepository chatMessageRepository;
    private final CardService cardService;
    private final LibraryService libraryService;
    private final PresenceService presenceService;

    @Autowired(required = false)
    private ChatClient chatClient;

    @Autowired(required = false)
    private String systemPrompt;

    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public AiChatServiceImpl(ChatMessageRepository chatMessageRepository,
                             CardService cardService,
                             LibraryService libraryService,
                             PresenceService presenceService) {
        this.chatMessageRepository = chatMessageRepository;
        this.cardService = cardService;
        this.libraryService = libraryService;
        this.presenceService = presenceService;
    }

    @Override
    @Transactional
    public String chat(Long studentId, String message) {
        saveMessage(studentId, "user", message);

        String aiResponse;

        if (chatClient != null) {
            // ===== AI 模式：语义理解优先 =====
            // 不经过关键词匹配，直接交给 Spring AI 做语义理解 + Function Calling
            // "续期" = "延长借书时间" = "延期" → AI 自动调用 extendBook
            // "充钱" = "充值" = "给卡里加钱" → AI 自动调用 topUpCard
            // 所有语义等价关系由 System Prompt 定义，AI 自行判断
            try {
                String context = buildDataContext(studentId);
                String fullPrompt = context + "\n【学生消息】" + message;
                aiResponse = chatClient.prompt()
                        .system(systemPrompt != null ? systemPrompt : "你是校园智能助理")
                        .user(fullPrompt)
                        .call()
                        .content();
                if (aiResponse == null || aiResponse.isBlank()) {
                    aiResponse = "抱歉，我暂时无法处理这个请求，请稍后重试。";
                }
            } catch (Exception e) {
                log.error("Spring AI 调用失败: {}", e.getMessage());
                // AI 不可用时降级为关键词匹配
                String fallback = parseLocalActions(studentId, message);
                aiResponse = fallback != null ? fallback : generateDemoResponse(message);
            }
        } else {
            // ===== Demo 模式：关键词匹配兜底 =====
            String local = parseLocalActions(studentId, message);
            aiResponse = local != null ? local : generateDemoResponse(message);
        }

        saveMessage(studentId, "assistant", aiResponse);
        return aiResponse;
    }

    // ===== 本地操作意图解析 =====

    private String parseLocalActions(Long studentId, String text) {
        // 充值 — "充值" = "充钱" = "充卡" = "存钱" = "给卡加钱"
        Pattern p = Pattern.compile("(?:充值|冲值|充钱|充卡|存钱|给卡加钱|充|冲)(?:一卡通)?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:元)?");
        Matcher m = p.matcher(text.trim());
        if (m.find()) {
            try {
                BigDecimal amount = new BigDecimal(m.group(1));
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    cardService.topUp(studentId, amount);
                    return String.format("🌟 已成功为一卡通在线充值 **¥%s** 元。", amount.toPlainString());
                }
            } catch (Exception e) { log.warn("充值失败", e); }
        }

        // 续借 / 延期 / 延长 — 语义等价，统一调用 extendBook
        if (text.contains("续期") || text.contains("续借") || text.contains("延期")
                || text.contains("延长") || text.contains("推迟") || text.contains("晚点还")
                || text.contains("还想再看")) {
            try {
                var books = libraryService.getBooks(studentId);
                if (!books.isEmpty()) {
                    int days = 14; // 默认延期14天
                    Pattern dayPattern = Pattern.compile("(\\d+)\\s*(?:天|日)");
                    Matcher dayMatcher = dayPattern.matcher(text);
                    if (dayMatcher.find()) {
                        days = Math.min(Math.max(Integer.parseInt(dayMatcher.group(1)), 1), 90);
                    }
                    var book = books.get(0);
                    var result = libraryService.extendBook(book.getId(), studentId, days);
                    return String.format("📚 《%s》已成功延期 %d 天！新归还日期：%s（累计延期%d次）",
                            result.getTitle(), days, result.getDueDate(), result.getRenewed());
                }
                return "你当前没有在借图书哦～";
            } catch (Exception e) { return "延期失败：" + e.getMessage(); }
        }

        // 报名 — "报名" = "参加" = "加入" = "我想去"
        if (text.contains("报名") || text.contains("参加活动") || text.contains("音乐") || text.contains("草坪")) {
            try {
                presenceService.joinEvent(3L, studentId);
                return "🎵 已成功预订「草坪音乐会」席位！✨";
            } catch (Exception ignored) {}
        }

        // 签到 / 入座
        if (text.contains("签到") || text.contains("入座")) {
            try { return presenceService.checkin(studentId); } catch (Exception ignored) {}
        }

        // 释放座位 — "释放" = "退座" = "签退" = "取消" = "不坐了" = "离开" = "走啦"
        if (text.contains("释放") || text.contains("退座") || text.contains("签退")
                || text.contains("取消") || text.contains("不坐") || text.contains("离开座位")) {
            try {
                var seats = libraryService.getSeats();
                var occ = seats.stream().filter(s -> "occupied".equals(s.getStatus())).findFirst();
                if (occ.isPresent()) {
                    libraryService.releaseSeat(occ.get().getId(), studentId);
                    return "🚪 已释放座位 " + occ.get().getSeatCode() + "，欢迎下次再来！";
                }
                return "你当前没有预约座位哦～";
            } catch (Exception e) { return "释放失败：" + e.getMessage(); }
        }

        // 预约座位 — "预约" = "占座" = "选座" = "订座" = "找位子" = "我要自习" = "有空位吗"
        if (text.contains("预约") || text.contains("占座") || text.contains("选座") || text.contains("订座")
                || text.contains("找位子") || text.contains("有空位") || text.contains("我要自习")
                || text.contains("座位") && (text.contains("帮") || text.contains("要") || text.contains("想"))) {
            try {
                String floor = null;
                if (text.contains("1楼") || text.contains("1F") || text.contains("一楼") || text.contains("1层")) floor = "1F";
                else if (text.contains("2楼") || text.contains("2F") || text.contains("二楼") || text.contains("2层")) floor = "2F";
                else if (text.contains("3楼") || text.contains("3F") || text.contains("三楼") || text.contains("3层")) floor = "3F";

                var seats = floor != null ? libraryService.getSeatsByFloor(floor) : libraryService.getSeats();
                var avail = seats.stream().filter(s -> "available".equals(s.getStatus())).findFirst();
                if (avail.isEmpty()) {
                    return "📌 " + (floor != null ? floor + "层" : "") + "暂无空闲座位，请尝试其他楼层。";
                }
                var booked = libraryService.bookSeat(avail.get().getId(), studentId);
                return String.format("✅ 座位预约成功！\n📍 %s层 %s号\n🕐 %s",
                        booked.getFloorArea(), booked.getSeatCode(), booked.getTimeLabel());
            } catch (Exception e) {
                log.warn("预约座位失败: {}", e.getMessage());
                return "预约失败：" + e.getMessage();
            }
        }

        return null;
    }

    // ===== 辅助方法 =====

    private String buildDataContext(Long studentId) {
        StringBuilder ctx = new StringBuilder("【学生当前数据】\n");
        try {
            var card = cardService.getCardInfo(studentId);
            ctx.append("- 姓名：").append(card.getStudentName())
                    .append(" | 余额：¥").append(card.getBalance().toPlainString()).append("\n");
        } catch (Exception ignored) {}
        try {
            var books = libraryService.getBooks(studentId);
            if (!books.isEmpty()) {
                ctx.append("- 在借图书：");
                for (var b : books) {
                    ctx.append("《").append(b.getTitle()).append("》(剩").append(b.getDaysRemaining()).append("天) ");
                }
                ctx.append("\n");
            }
        } catch (Exception ignored) {}
        try {
            String seat = libraryService.getCurrentBookedSeatCode(studentId);
            if (seat != null) ctx.append("- 当前座位：").append(seat).append("\n");
        } catch (Exception ignored) {}
        return ctx.toString();
    }

    private void saveMessage(Long studentId, String role, String content) {
        chatMessageRepository.save(ChatMessage.builder()
                .studentId(studentId).role(role).content(content)
                .createdAt(LocalDateTime.now()).build());
    }

    @Override
    public List<ChatMessageDTO> getHistory(Long studentId) {
        return chatMessageRepository.findByStudentIdOrderByCreatedAtAsc(studentId)
                .stream().map(m -> ChatMessageDTO.builder()
                        .id(m.getId()).role(m.getRole()).content(m.getContent())
                        .createdAt(m.getCreatedAt()).build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearHistory(Long studentId) {
        chatMessageRepository.deleteByStudentId(studentId);
        chatMessageRepository.save(ChatMessage.builder()
                .studentId(studentId).role("assistant")
                .content("已成功校准系统时钟。你可以重新向我提问。✨")
                .createdAt(LocalDateTime.now()).build());
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void cleanExpiredMessages() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        int deleted = chatMessageRepository.deleteByCreatedAtBefore(cutoff);
        if (deleted > 0) {
            log.info("聊天记录清理：已删除 {} 条超过3天的消息", deleted);
        }
    }

    private String generateDemoResponse(String msg) {
        String lower = msg.toLowerCase();
        if (lower.contains("课程") || lower.contains("课")) return "📅 **今日课程**\n- 08:30 人工智慧与脑机接口 (新星科技楼 402)\n- 14:00 经典中国哲学导读 (综合报告厅)";
        if (lower.contains("充") || lower.contains("卡") || lower.contains("余额")) return "💳 你可以对我说「充值50元」即可完成实时充值。";
        if (lower.contains("自习") || lower.contains("签到")) return "🏫 📍 图书馆1F层-独学空间舱 (专心修仙中 ✍️)";
        if (lower.contains("图书") || lower.contains("借")) return "📚 你当前借阅了 2 本学术书籍，点击「一键续期」可延长借阅。";
        if (lower.contains("绩点") || lower.contains("gpa")) return "📈 GPA 3.85/4.0 | 学分 98/150 | 专业前 5%";
        return "🪄 试试：查课表 / 充值一卡通 / 预约座位 / 续借图书 / 校园活动";
    }
}
