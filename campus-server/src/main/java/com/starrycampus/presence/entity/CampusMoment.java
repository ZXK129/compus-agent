package com.starrycampus.presence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_campus_moment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusMoment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    @Column(nullable = false, length = 500)
    private String title;
    @Column(nullable = false, length = 50)
    private String tag;
    @Column(nullable = false, length = 200)
    private String location;
    @Column(nullable = false)
    private Integer likes;
    @Column(nullable = false)
    private Byte joined;
    @Column(name = "max_attendees")
    private Integer maxAttendees;
    @Column(name = "current_attendees")
    private Integer currentAttendees;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (likes == null) likes = 0;
        if (joined == null) joined = 0;
        if (currentAttendees == null) currentAttendees = 0;
    }
}
