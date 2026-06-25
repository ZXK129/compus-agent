package com.starrycampus.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private Long id;
    private String name;
    private String code;
    private String instructor;
    private String courseTime;
    private Byte weekday;
    private String location;
    private Integer credits;
    private String category;
    private String color;
    private String categoryLabel;
    private String colorClass;
}
