package com.starrycampus.academic.dto;

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
public class AcademicProfileDTO {
    private BigDecimal gpa;
    private BigDecimal maxGpa;
    private Integer creditsEarned;
    private Integer creditsRequired;
    private Double progressPercent;
    private List<SubjectStrengthDTO> strengths;
}
