package com.starrycampus.presence.controller;

import com.starrycampus.common.base.ApiResponse;
import com.starrycampus.presence.dto.CampusMomentDTO;
import com.starrycampus.presence.dto.MomentCreateRequest;
import com.starrycampus.presence.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/presence")
public class PresenceController {

    private final PresenceService presenceService;
    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public PresenceController(PresenceService presenceService) { this.presenceService = presenceService; }

    @GetMapping("/moments")
    public ApiResponse<List<CampusMomentDTO>> getMoments() {
        return ApiResponse.success(presenceService.getMoments());
    }

    @PostMapping("/moments")
    public ApiResponse<CampusMomentDTO> createMoment(@Valid @RequestBody MomentCreateRequest req) {
        return ApiResponse.success("发布成功", presenceService.createMoment(defaultStudentId, req.getTitle(), req.getTag(), req.getLocation()));
    }

    @PostMapping("/moments/{id}/join")
    public ApiResponse<CampusMomentDTO> joinEvent(@PathVariable Long id) {
        return ApiResponse.success("报名成功", presenceService.joinEvent(id, defaultStudentId));
    }

    @GetMapping("/checkin-status")
    public ApiResponse<String> getCheckinStatus() {
        return ApiResponse.success(presenceService.getCheckinStatus(defaultStudentId));
    }

    @PostMapping("/checkin")
    public ApiResponse<String> checkin() {
        return ApiResponse.success("签到成功", presenceService.checkin(defaultStudentId));
    }
}
