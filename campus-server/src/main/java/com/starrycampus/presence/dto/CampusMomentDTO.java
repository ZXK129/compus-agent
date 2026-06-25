package com.starrycampus.presence.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusMomentDTO {
    private Long id;
    private String title;
    private String tag;
    private String location;
    private Integer likes;
    private Byte joined;
    private Integer maxAttendees;
    private Integer currentAttendees;
    private String timeLabel;
    private LocalDateTime createdAt;
}
