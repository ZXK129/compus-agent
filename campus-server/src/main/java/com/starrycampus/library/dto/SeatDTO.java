package com.starrycampus.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDTO {
    private Long id;
    private String seatCode;
    private String floorArea;
    private String status;
    private String statusLabel;
    private LocalDateTime bookedStart;
    private LocalDateTime bookedEnd;
    private String timeLabel;
}
