package com.starrycampus.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBookDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private String coverColor;
    private LocalDate dueDate;
    private Integer daysRemaining;
    private Integer progress;
    private Byte renewed;
}
