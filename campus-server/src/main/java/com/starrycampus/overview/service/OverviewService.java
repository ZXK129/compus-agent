package com.starrycampus.overview.service;

import com.starrycampus.overview.dto.PersonalOverviewDTO;

public interface OverviewService {
    PersonalOverviewDTO getPersonalOverview(Long studentId);
}
