package com.starrycampus.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDTO {
    private Long id;
    private Long studentId;
    private BigDecimal balance;
    private String cardNo;
    private String studentName;
    private String studentNo;
    private List<TransactionDTO> recentTransactions;
}
