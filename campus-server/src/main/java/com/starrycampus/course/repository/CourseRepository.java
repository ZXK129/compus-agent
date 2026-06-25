package com.starrycampus.course.repository;

import com.starrycampus.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByWeekday(Byte weekday);
    List<Course> findAllByOrderByWeekdayAscCourseTimeAsc();
}
