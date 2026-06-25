package com.starrycampus.course.controller;

import com.starrycampus.common.base.ApiResponse;
import com.starrycampus.course.dto.CourseDTO;
import com.starrycampus.course.service.CourseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<List<CourseDTO>> getCourses(@RequestParam(required = false) Byte weekday) {
        List<CourseDTO> courses = (weekday != null)
                ? courseService.getCoursesByWeekday(weekday)
                : courseService.getAllCourses();
        return ApiResponse.success(courses);
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseDTO> getCourse(@PathVariable Long id) {
        return ApiResponse.success(courseService.getCourseById(id));
    }

    @PostMapping("/{id}/checkin")
    public ApiResponse<String> checkin(@PathVariable Long id) {
        return ApiResponse.success("签到成功", courseService.checkin(id, defaultStudentId));
    }
}
