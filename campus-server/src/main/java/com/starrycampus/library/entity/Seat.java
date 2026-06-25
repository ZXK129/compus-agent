package com.starrycampus.library.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_seat")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "seat_code", nullable = false, unique = true, length = 20)
    private String seatCode;
    @Column(name = "floor_area", nullable = false, length = 20)
    private String floorArea;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "student_id")
    private Long studentId;
    @Column(name = "booked_start")
    private LocalDateTime bookedStart;
    @Column(name = "booked_end")
    private LocalDateTime bookedEnd;
}
