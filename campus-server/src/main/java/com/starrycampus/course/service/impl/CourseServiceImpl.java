package com.starrycampus.course.service.impl;

import com.starrycampus.course.dto.CourseDTO;
import com.starrycampus.course.entity.Course;
import com.starrycampus.course.repository.CourseRepository;
import com.starrycampus.course.service.CourseService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAllByOrderByWeekdayAscCourseTimeAsc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CourseDTO> getCoursesByWeekday(Byte weekday) {
        return courseRepository.findByWeekday(weekday)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + id));
        return toDTO(course);
    }

    @Override
    public String checkin(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在: " + courseId));
        return String.format("📍 **课堂签到成功**\n课程：%s\n地点：%s\n时间：%s ✅",
                course.getName(), course.getLocation(), course.getCourseTime());
    }

    private CourseDTO toDTO(Course course) {
        String categoryLabel = switch (course.getCategory()) {
            case "Major" -> "核心必修";
            case "Elective" -> "公共选修";
            case "General" -> "通识必修";
            default -> course.getCategory();
        };
        String colorClass = switch (course.getCategory()) {
            case "Major" -> "bg-neutral-900 text-white";
            case "Elective" -> "bg-neutral-100 text-neutral-800 border border-neutral-200/60";
            case "General" -> "bg-neutral-50 text-neutral-600 border border-neutral-200/40";
            default -> "bg-neutral-50 text-neutral-600";
        };
        return CourseDTO.builder()
                .id(course.getId()).name(course.getName()).code(course.getCode())
                .instructor(course.getInstructor()).courseTime(course.getCourseTime())
                .weekday(course.getWeekday()).location(course.getLocation())
                .credits(course.getCredits()).category(course.getCategory())
                .color(course.getColor()).categoryLabel(categoryLabel).colorClass(colorClass)
                .build();
    }
}
