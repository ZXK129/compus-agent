package com.starrycampus.overview.controller;

import com.starrycampus.common.base.ApiResponse;
import com.starrycampus.overview.dto.PersonalOverviewDTO;
import com.starrycampus.overview.service.OverviewService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
public class OverviewController {

    private final OverviewService overviewService;

    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/personal")
    public ApiResponse<PersonalOverviewDTO> getPersonalOverview() {
        PersonalOverviewDTO overview = overviewService.getPersonalOverview(defaultStudentId);
        return ApiResponse.success("个人总览数据加载完成", overview);
    }
}
