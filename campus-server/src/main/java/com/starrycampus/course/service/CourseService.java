package com.starrycampus.course.service;

import com.starrycampus.course.dto.CourseDTO;
import java.util.List;

public interface CourseService {
    List<CourseDTO> getAllCourses();
    List<CourseDTO> getCoursesByWeekday(Byte weekday);
    CourseDTO getCourseById(Long id);
    String checkin(Long courseId, Long studentId);
}
