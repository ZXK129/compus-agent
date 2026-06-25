package com.starrycampus.card.service;

import com.starrycampus.card.dto.CardDTO;
import com.starrycampus.card.dto.TransactionDTO;
import java.math.BigDecimal;
import java.util.List;

public interface CardService {
    CardDTO getCardInfo(Long studentId);
    List<TransactionDTO> getTransactions(Long studentId, Integer limit);
    CardDTO topUp(Long studentId, BigDecimal amount);
}
