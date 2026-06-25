package com.starrycampus.card.service.impl;

import com.starrycampus.card.dto.CardDTO;
import com.starrycampus.card.dto.TransactionDTO;
import com.starrycampus.card.entity.Card;
import com.starrycampus.card.entity.Transaction;
import com.starrycampus.card.repository.CardRepository;
import com.starrycampus.card.repository.TransactionRepository;
import com.starrycampus.card.service.CardService;
import com.starrycampus.common.entity.Student;
import com.starrycampus.academic.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final StudentRepository studentRepository;

    public CardServiceImpl(CardRepository cardRepository,
                           TransactionRepository transactionRepository,
                           StudentRepository studentRepository) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public CardDTO getCardInfo(Long studentId) {
        Card card = cardRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("一卡通不存在: studentId=" + studentId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));
        List<TransactionDTO> transactions = getTransactions(studentId, 3);
        return CardDTO.builder()
                .id(card.getId()).studentId(card.getStudentId())
                .balance(card.getBalance()).cardNo(card.getCardNo())
                .studentName(student.getName()).studentNo(student.getStudentNo())
                .recentTransactions(transactions).build();
    }

    @Override
    public List<TransactionDTO> getTransactions(Long studentId, Integer limit) {
        Card card = cardRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("一卡通不存在"));
        List<Transaction> txList = (limit != null && limit > 0)
                ? transactionRepository.findTop3ByCardIdOrderByCreatedAtDesc(card.getId())
                : transactionRepository.findByCardIdOrderByCreatedAtDesc(card.getId());
        return txList.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardDTO topUp(Long studentId, BigDecimal amount) {
        Card card = cardRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("一卡通不存在"));
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
        Transaction tx = Transaction.builder()
                .cardId(card.getId()).item("快捷充值额度").amount(amount)
                .type("income").category("utility").location("智能体线上冲值终端")
                .createdAt(LocalDateTime.now()).build();
        transactionRepository.save(tx);
        return getCardInfo(studentId);
    }

    private TransactionDTO toDTO(Transaction tx) {
        String timeLabel = formatTimeLabel(tx.getCreatedAt());
        return TransactionDTO.builder()
                .id(tx.getId()).item(tx.getItem()).amount(tx.getAmount())
                .type(tx.getType()).category(tx.getCategory())
                .location(tx.getLocation()).createdAt(tx.getCreatedAt())
                .timeLabel(timeLabel).build();
    }

    private String formatTimeLabel(LocalDateTime time) {
        if (time == null) return "";
        LocalDateTime now = LocalDateTime.now();
        if (time.toLocalDate().equals(now.toLocalDate()))
            return "今日 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        if (time.toLocalDate().equals(now.toLocalDate().minusDays(1)))
            return "昨日 " + time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}
