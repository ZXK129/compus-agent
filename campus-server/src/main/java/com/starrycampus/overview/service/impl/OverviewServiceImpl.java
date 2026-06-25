package com.starrycampus.overview.service.impl;

import com.starrycampus.common.entity.Student;
import com.starrycampus.course.dto.CourseDTO;
import com.starrycampus.course.entity.Course;
import com.starrycampus.course.repository.CourseRepository;
import com.starrycampus.library.dto.LibraryBookDTO;
import com.starrycampus.library.dto.SeatDTO;
import com.starrycampus.library.entity.LibraryBook;
import com.starrycampus.library.entity.Seat;
import com.starrycampus.library.repository.LibraryBookRepository;
import com.starrycampus.library.repository.SeatRepository;
import com.starrycampus.overview.dto.PersonalOverviewDTO;
import com.starrycampus.overview.service.OverviewService;
import com.starrycampus.academic.repository.StudentRepository;
import com.starrycampus.presence.dto.CampusMomentDTO;
import com.starrycampus.presence.entity.CampusMoment;
import com.starrycampus.presence.repository.CampusMomentRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OverviewServiceImpl implements OverviewService {

    private static final int MAX_BORROW_LIMIT = 5;

    private final StudentRepository studentRepository;
    private final SeatRepository seatRepository;
    private final LibraryBookRepository bookRepository;
    private final CampusMomentRepository momentRepository;
    private final CourseRepository courseRepository;

    public OverviewServiceImpl(StudentRepository studentRepository,
                               SeatRepository seatRepository,
                               LibraryBookRepository bookRepository,
                               CampusMomentRepository momentRepository,
                               CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.seatRepository = seatRepository;
        this.bookRepository = bookRepository;
        this.momentRepository = momentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public PersonalOverviewDTO getPersonalOverview(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在: " + studentId));

        // 1. 当前预约座位
        SeatDTO bookedSeat = seatRepository.findByStudentIdAndStatus(studentId, "occupied")
                .map(this::toSeatDTO).orElse(null);

        // 2. 在借图书
        List<LibraryBookDTO> borrowedBooks = bookRepository.findByStudentId(studentId)
                .stream().map(this::toBookDTO).collect(Collectors.toList());

        // 3. 已报名活动
        List<CampusMomentDTO> joinedEvents = momentRepository
                .findByJoinedOrderByCreatedAtDesc((byte) 1)
                .stream().map(this::toMomentDTO).collect(Collectors.toList());

        // 4. 今日课程
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<CourseDTO> todayCourses;
        String todayLabel;
        if (today.getValue() >= 1 && today.getValue() <= 5) {
            byte weekday = (byte) today.getValue(); // 1=Mon ... 5=Fri
            todayCourses = courseRepository.findByWeekday(weekday)
                    .stream().map(this::toCourseDTO).collect(Collectors.toList());
            todayLabel = formatDayOfWeek(today);
        } else {
            todayCourses = Collections.emptyList();
            todayLabel = "今天是周末，暂无课程安排 🌿";
        }

        return PersonalOverviewDTO.builder()
                .studentName(student.getName())
                .studentNo(student.getStudentNo())
                .department(student.getDepartment())
                .bookedSeat(bookedSeat)
                .borrowedBooks(borrowedBooks)
                .borrowedCount(borrowedBooks.size())
                .maxBorrowLimit(MAX_BORROW_LIMIT)
                .joinedEvents(joinedEvents)
                .todayCourses(todayCourses)
                .todayLabel(todayLabel)
                .semesterInfo("第 16 教学周 / 春季学期")
                .build();
    }

    // ===== DTO 转换 =====

    private SeatDTO toSeatDTO(Seat seat) {
        String timeLabel = "";
        if (seat.getBookedStart() != null && seat.getBookedEnd() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            timeLabel = "今日 " + seat.getBookedStart().format(fmt)
                    + " - " + seat.getBookedEnd().format(fmt);
        }
        return SeatDTO.builder()
                .id(seat.getId()).seatCode(seat.getSeatCode())
                .floorArea(seat.getFloorArea()).status(seat.getStatus())
                .statusLabel("occupied".equals(seat.getStatus()) ? "占用" : "空闲")
                .bookedStart(seat.getBookedStart()).bookedEnd(seat.getBookedEnd())
                .timeLabel(timeLabel).build();
    }

    private LibraryBookDTO toBookDTO(LibraryBook book) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), book.getDueDate());
        return LibraryBookDTO.builder()
                .id(book.getId()).title(book.getTitle()).author(book.getAuthor())
                .isbn(book.getIsbn()).coverColor(book.getCoverColor())
                .dueDate(book.getDueDate()).daysRemaining((int) Math.max(0, days))
                .progress(book.getProgress()).renewed(book.getRenewed()).build();
    }

    private CampusMomentDTO toMomentDTO(CampusMoment m) {
        return CampusMomentDTO.builder()
                .id(m.getId()).title(m.getTitle()).tag(m.getTag())
                .location(m.getLocation()).likes(m.getLikes()).joined(m.getJoined())
                .maxAttendees(m.getMaxAttendees()).currentAttendees(m.getCurrentAttendees())
                .timeLabel(formatTimeAgo(m.getCreatedAt())).createdAt(m.getCreatedAt()).build();
    }

    private CourseDTO toCourseDTO(Course course) {
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

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        long minutes = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 60) return minutes + "分钟前";
        if (minutes < 1440) return (minutes / 60) + "小时前";
        return (minutes / 1440) + "天前";
    }

    private String formatDayOfWeek(DayOfWeek day) {
        return switch (day) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            default -> "";
        };
    }
}
