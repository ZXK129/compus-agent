package com.starrycampus.academic.service.impl;

import com.starrycampus.academic.dto.AcademicProfileDTO;
import com.starrycampus.academic.dto.SubjectStrengthDTO;
import com.starrycampus.academic.repository.StudentRepository;
import com.starrycampus.academic.service.AcademicService;
import com.starrycampus.common.entity.Student;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class AcademicServiceImpl implements AcademicService {

    private final StudentRepository studentRepository;

    public AcademicServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public AcademicProfileDTO getProfile(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));

        double progressPercent = student.getCreditsRequired() > 0
                ? (double) student.getCreditsEarned() / student.getCreditsRequired() * 100 : 0;

        List<SubjectStrengthDTO> strengths = Arrays.asList(
                SubjectStrengthDTO.builder().subject("算法逻辑与数理脑机").val(95).build(),
                SubjectStrengthDTO.builder().subject("分布式开发与架构").val(88).build(),
                SubjectStrengthDTO.builder().subject("经典中国哲学思辨").val(92).build(),
                SubjectStrengthDTO.builder().subject("交互UI与计算社会学").val(90).build()
        );

        return AcademicProfileDTO.builder()
                .gpa(student.getGpa()).maxGpa(new BigDecimal("4.0"))
                .creditsEarned(student.getCreditsEarned()).creditsRequired(student.getCreditsRequired())
                .progressPercent(Math.round(progressPercent * 10.0) / 10.0)
                .strengths(strengths).build();
    }
}
