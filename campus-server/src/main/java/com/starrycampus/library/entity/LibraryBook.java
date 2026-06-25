package com.starrycampus.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_library_book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    @Column(nullable = false, length = 300)
    private String title;
    @Column(nullable = false, length = 200)
    private String author;
    @Column(nullable = false, length = 20)
    private String isbn;
    @Column(name = "cover_color", nullable = false, length = 30)
    private String coverColor;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(nullable = false)
    private Integer progress;
    @Column(nullable = false)
    private Byte renewed;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
