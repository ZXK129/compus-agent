package com.starrycampus.academic.controller;

import com.starrycampus.academic.dto.AcademicProfileDTO;
import com.starrycampus.academic.service.AcademicService;
import com.starrycampus.common.base.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final AcademicService academicService;
    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public AcademicController(AcademicService academicService) { this.academicService = academicService; }

    @GetMapping("/profile")
    public ApiResponse<AcademicProfileDTO> getProfile() {
        return ApiResponse.success(academicService.getProfile(defaultStudentId));
    }
}
