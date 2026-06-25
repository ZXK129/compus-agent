package com.starrycampus.academic.service;

import com.starrycampus.academic.dto.AcademicProfileDTO;

public interface AcademicService {
    AcademicProfileDTO getProfile(Long studentId);
}
