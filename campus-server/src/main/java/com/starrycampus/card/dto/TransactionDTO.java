package com.starrycampus.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private String item;
    private BigDecimal amount;
    private String type;
    private String category;
    private String location;
    private LocalDateTime createdAt;
    private String timeLabel;
}
