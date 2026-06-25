package com.starrycampus.course.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = false, length = 20)
    private String code;
    @Column(nullable = false, length = 100)
    private String instructor;
    @Column(name = "course_time", nullable = false, length = 50)
    private String courseTime;
    @Column(nullable = false)
    private Byte weekday;
    @Column(nullable = false, length = 200)
    private String location;
    @Column(nullable = false)
    private Integer credits;
    @Column(nullable = false, length = 20)
    private String category;
    @Column(nullable = false, length = 30)
    private String color;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
