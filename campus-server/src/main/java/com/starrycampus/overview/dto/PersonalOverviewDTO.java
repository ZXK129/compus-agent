package com.starrycampus.overview.dto;

import com.starrycampus.course.dto.CourseDTO;
import com.starrycampus.library.dto.LibraryBookDTO;
import com.starrycampus.library.dto.SeatDTO;
import com.starrycampus.presence.dto.CampusMomentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 个人总览聚合响应 — 一接口返回全部个人相关信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalOverviewDTO {

    /** 学生基本信息 */
    private String studentName;
    private String studentNo;
    private String department;

    /** 当前预约座位（null 表示未预约） */
    private SeatDTO bookedSeat;

    /** 在借图书列表 */
    private List<LibraryBookDTO> borrowedBooks;
    private int borrowedCount;
    private int maxBorrowLimit;

    /** 已报名的活动列表 */
    private List<CampusMomentDTO> joinedEvents;

    /** 今日课程 */
    private List<CourseDTO> todayCourses;
    private String todayLabel;
    private String semesterInfo;
}
