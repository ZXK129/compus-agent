package com.starrycampus.chat.tool;

import com.starrycampus.academic.service.AcademicService;
import com.starrycampus.card.service.CardService;
import com.starrycampus.chat.tool.function.ToolInputs.*;
import com.starrycampus.course.service.CourseService;
import com.starrycampus.library.dto.SeatDTO;
import com.starrycampus.library.service.LibraryService;
import com.starrycampus.overview.service.OverviewService;
import com.starrycampus.presence.service.PresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring AI Function Calling 工具配置
 * 每个 @Bean 定义一个 FunctionCallback，由 ChatClient 自动注册
 */
@Configuration
public class CampusToolConfig {

    private static final Logger log = LoggerFactory.getLogger(CampusToolConfig.class);

    @Value("${campus.demo-student-id:1}")
    private Long studentId;

    // ===== 查询类工具 =====

    @Bean
    public FunctionCallback queryCoursesTool(CourseService courseService) {
        return FunctionCallback.builder()
                .description("查询课程安排。可选参数 weekday(1=周一~5=周五)，不传返回全部课程")
                .function("queryCourses", (WeekdayInput input) -> {
                    if (input != null && input.weekday() != null) {
                        byte wd = input.weekday().byteValue();
                        var courses = courseService.getCoursesByWeekday(wd);
                        if (courses.isEmpty()) return "当天没有课程安排 🎉";
                        return "周" + wd + "课程：\n" + courses.stream()
                                .map(c -> String.format("- %s %s | %s | %s (%d学分)",
                                        c.getCourseTime(), c.getName(), c.getInstructor(),
                                        c.getLocation(), c.getCredits()))
                                .collect(Collectors.joining("\n"));
                    }
                    var all = courseService.getAllCourses();
                    return all.stream()
                            .map(c -> String.format("[周%d] %s %s | %s | %s",
                                    c.getWeekday(), c.getCourseTime(), c.getName(),
                                    c.getInstructor(), c.getLocation()))
                            .collect(Collectors.joining("\n"));
                })
                .inputType(WeekdayInput.class)
                .build();
    }

    @Bean
    public FunctionCallback checkinCourseTool(CourseService courseService) {
        return FunctionCallback.builder()
                .description("对指定课程进行签到。courseId 为课程ID")
                .function("checkinCourse", (CourseIdInput input) ->
                        courseService.checkin(input.courseId(), studentId))
                .inputType(CourseIdInput.class)
                .build();
    }

    @Bean
    public FunctionCallback queryCardTool(CardService cardService) {
        return FunctionCallback.builder()
                .description("查询一卡通余额、卡号和最近3条消费记录")
                .function("queryCard", (EmptyInput input) -> {
                    var card = cardService.getCardInfo(studentId);
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("💳 余额：¥%s | 卡号：%s\n",
                            card.getBalance().toPlainString(), card.getCardNo()));
                    var txns = card.getRecentTransactions();
                    if (txns != null && !txns.isEmpty()) {
                        sb.append("最近消费：\n");
                        txns.forEach(t -> sb.append(String.format("- %s ¥%s (%s)\n",
                                t.getItem(), t.getAmount().toPlainString(), t.getLocation())));
                    }
                    return sb.toString().trim();
                })
                .inputType(EmptyInput.class)
                .build();
    }

    @Bean
    public FunctionCallback topUpCardTool(CardService cardService) {
        return FunctionCallback.builder()
                .description("为一卡通充值。amount 为充值金额（元），必须大于0")
                .function("topUpCard", (AmountInput input) -> {
                    BigDecimal amount = BigDecimal.valueOf(input.amount());
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) return "充值金额必须大于0元";
                    var card = cardService.topUp(studentId, amount);
                    return String.format("✅ 充值成功！¥%s 已到账，当前余额 ¥%s",
                            amount.toPlainString(), card.getBalance().toPlainString());
                })
                .inputType(AmountInput.class)
                .build();
    }

    @Bean
    public FunctionCallback queryBooksTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("查询当前在借图书列表：书名、作者、归还日期、剩余天数、阅读进度")
                .function("queryBooks", (EmptyInput input) -> {
                    var books = libraryService.getBooks(studentId);
                    if (books.isEmpty()) return "当前没有在借图书 📖";
                    return "在借(" + books.size() + "/" + libraryService.getMaxBorrowLimit() + ")：\n"
                            + books.stream().map(b -> String.format("- 《%s》%s | 归还:%s | 剩%d天 | 已读%d%%%s",
                                    b.getTitle(), b.getAuthor(), b.getDueDate(), b.getDaysRemaining(),
                                    b.getProgress(), b.getRenewed() == 1 ? "(已续借)" : ""))
                            .collect(Collectors.joining("\n"));
                })
                .inputType(EmptyInput.class)
                .build();
    }

    @Bean
    public FunctionCallback renewBookTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("续借指定图书(+7天)。bookId为图书ID，每本书只能续借一次")
                .function("renewBook", (BookIdInput input) -> {
                    try {
                        var book = libraryService.renewBook(input.bookId(), studentId);
                        return String.format("✅ 续借成功！《%s》归还日期延长至 %s", book.getTitle(), book.getDueDate());
                    } catch (RuntimeException e) { return "续借失败：" + e.getMessage(); }
                })
                .inputType(BookIdInput.class)
                .build();
    }

    @Bean
    public FunctionCallback extendBookTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("延长图书借阅期限。需提供bookId(图书ID)和days(延期天数，1-90)。可多次延期不限制次数")
                .function("extendBook", (ExtendInput input) -> {
                    try {
                        int days = input.days() != null ? input.days() : 14;
                        var book = libraryService.extendBook(input.bookId(), studentId, days);
                        return String.format("✅ 延期成功！《%s》归还日期延长至 %s（+%d天，累计延期%d次）",
                                book.getTitle(), book.getDueDate(), days, book.getRenewed());
                    } catch (RuntimeException e) { return "延期失败：" + e.getMessage(); }
                })
                .inputType(ExtendInput.class)
                .build();
    }

    @Bean
    public FunctionCallback borrowBookTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("借阅新书。需提供isbn、title、author。最多借5本，有逾期书不能借")
                .function("borrowBook", (BorrowInput input) -> {
                    try {
                        var book = libraryService.borrowBook(input.isbn(), input.title(), input.author(), studentId);
                        return String.format("✅ 借阅成功！《%s》归还日期：%s（30天）", book.getTitle(), book.getDueDate());
                    } catch (RuntimeException e) { return "借阅失败：" + e.getMessage(); }
                })
                .inputType(BorrowInput.class)
                .build();
    }

    // ===== 座位工具 =====

    @Bean
    public FunctionCallback querySeatsTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("查询图书馆自习座位空闲情况。可选 floor(1F/2F/3F)，不传返回全部")
                .function("querySeats", (FloorInput input) -> {
                    List<SeatDTO> seats;
                    if (input != null && input.floor() != null && !input.floor().isEmpty()) {
                        seats = libraryService.getSeatsByFloor(input.floor());
                    } else {
                        seats = libraryService.getSeats();
                    }
                    long avail = seats.stream().filter(s -> "available".equals(s.getStatus())).count();
                    String cur = libraryService.getCurrentBookedSeatCode(studentId);
                    return String.format("%d个座位：%d空闲 %d已占用 | %s | 预约:8:00-21:00",
                            seats.size(), avail, seats.size() - avail,
                            cur != null ? "你在:" + cur : "暂未预约");
                })
                .inputType(FloorInput.class)
                .build();
    }

    @Bean
    public FunctionCallback bookSeatTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("预约图书馆自习座位。可选floor参数(1F/2F/3F)指定楼层，自动分配空闲座位。不传则全局选座")
                .function("bookSeat", (FloorInput input) -> {
                    List<SeatDTO> seats;
                    String floor = input != null ? input.floor() : null;
                    if (floor != null && !floor.isEmpty()) {
                        seats = libraryService.getSeatsByFloor(floor);
                    } else {
                        seats = libraryService.getSeats();
                    }
                    var avail = seats.stream()
                            .filter(s -> "available".equals(s.getStatus()))
                            .findFirst();
                    if (avail.isEmpty()) {
                        return "抱歉，" + (floor != null ? floor + "层" : "") + "暂无空闲座位，请尝试其他楼层。";
                    }
                    try {
                        var booked = libraryService.bookSeat(avail.get().getId(), studentId);
                        return String.format("✅ 预约成功！%s层 %s | 时段：%s",
                                booked.getFloorArea(), booked.getSeatCode(), booked.getTimeLabel());
                    } catch (RuntimeException e) { return "预约失败：" + e.getMessage(); }
                })
                .inputType(FloorInput.class)
                .build();
    }

    @Bean
    public FunctionCallback releaseSeatTool(LibraryService libraryService) {
        return FunctionCallback.builder()
                .description("释放当前预约的座位，签退离座")
                .function("releaseSeat", (EmptyInput input) -> {
                    var seats = libraryService.getSeats();
                    var occ = seats.stream()
                            .filter(s -> "occupied".equals(s.getStatus()))
                            .findFirst();
                    if (occ.isEmpty()) return "你当前没有预约座位";
                    try {
                        libraryService.releaseSeat(occ.get().getId(), studentId);
                        return "✅ 已释放座位 " + occ.get().getSeatCode() + "，欢迎下次再来！";
                    } catch (RuntimeException e) { return "释放失败：" + e.getMessage(); }
                })
                .inputType(EmptyInput.class)
                .build();
    }

    // ===== 校园活动 =====

    @Bean
    public FunctionCallback queryEventsTool(PresenceService presenceService) {
        return FunctionCallback.builder()
                .description("查询校园动态和活动列表：标签、地点、参与人数、发布时间")
                .function("queryEvents", (EmptyInput input) -> {
                    var moments = presenceService.getMoments();
                    if (moments.isEmpty()) return "当前没有校园活动";
                    return moments.stream().map(m -> String.format("- [%s] %s 📍%s | 👍%d | 👥%d/%s | %s%s",
                            m.getTag(), m.getTitle(), m.getLocation(), m.getLikes(),
                            m.getCurrentAttendees(),
                            m.getMaxAttendees() != null ? m.getMaxAttendees().toString() : "∞",
                            m.getTimeLabel(),
                            m.getJoined() == 1 ? " ✅已报名" : ""))
                            .collect(Collectors.joining("\n"));
                })
                .inputType(EmptyInput.class)
                .build();
    }

    @Bean
    public FunctionCallback joinEventTool(PresenceService presenceService) {
        return FunctionCallback.builder()
                .description("报名参加校园活动。eventId为活动ID")
                .function("joinEvent", (EventIdInput input) -> {
                    try {
                        var moment = presenceService.joinEvent(input.eventId(), studentId);
                        return String.format("✅ 报名成功！已成功报名「%s」🎉", moment.getTitle());
                    } catch (RuntimeException e) { return "报名失败：" + e.getMessage(); }
                })
                .inputType(EventIdInput.class)
                .build();
    }

    @Bean
    public FunctionCallback createMomentTool(PresenceService presenceService) {
        return FunctionCallback.builder()
                .description("发布校园动态。需提供title(内容)、tag(标签)、location(位置)")
                .function("createMoment", (CreateMomentInput input) -> {
                    var moment = presenceService.createMoment(studentId, input.title(), input.tag(), input.location());
                    return String.format("✅ 动态发布成功！「%s」已发布到校园广场 📢", moment.getTitle());
                })
                .inputType(CreateMomentInput.class)
                .build();
    }

    // ===== 学业/总览 =====

    @Bean
    public FunctionCallback queryAcademicTool(AcademicService academicService) {
        return FunctionCallback.builder()
                .description("查询学业概况：GPA、学分进度、学科能力")
                .function("queryAcademic", (EmptyInput input) -> {
                    var p = academicService.getProfile(studentId);
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("📊 GPA: %.2f/%.1f | 学分: %d/%d (%d%%)\n",
                            p.getGpa(), p.getMaxGpa(), p.getCreditsEarned(),
                            p.getCreditsRequired(), p.getProgressPercent()));
                    if (p.getStrengths() != null) {
                        p.getStrengths().forEach(s ->
                                sb.append(String.format("- %s: %d/100\n", s.getSubject(), s.getVal())));
                    }
                    return sb.toString().trim();
                })
                .inputType(EmptyInput.class)
                .build();
    }

    @Bean
    public FunctionCallback personalOverviewTool(OverviewService overviewService) {
        return FunctionCallback.builder()
                .description("查询个人总览：当前座位、今日课程、在借图书、已报名活动、学业概览")
                .function("personalOverview", (EmptyInput input) -> {
                    var o = overviewService.getPersonalOverview(studentId);
                    StringBuilder sb = new StringBuilder();
                    sb.append("📋 ").append(o.getStudentName()).append(" | ")
                            .append(o.getStudentNo()).append(" | ").append(o.getDepartment()).append("\n\n");
                    sb.append("📍 座位：");
                    sb.append(o.getBookedSeat() != null
                            ? o.getBookedSeat().getFloorArea() + "层 " + o.getBookedSeat().getSeatCode()
                            : "未预约");
                    sb.append("\n📅 ").append(o.getTodayLabel()).append("：");
                    if (o.getTodayCourses().isEmpty()) sb.append("无课");
                    else o.getTodayCourses().forEach(c ->
                            sb.append(String.format("\n- %s %s | %s", c.getCourseTime(), c.getName(), c.getLocation())));
                    sb.append("\n📚 图书(").append(o.getBorrowedCount()).append("/").append(o.getMaxBorrowLimit()).append(")：");
                    if (o.getBorrowedBooks().isEmpty()) sb.append("无");
                    else o.getBorrowedBooks().forEach(b ->
                            sb.append(String.format("\n- 《%s》剩%d天", b.getTitle(), b.getDaysRemaining())));
                    sb.append("\n🎉 活动：");
                    if (o.getJoinedEvents().isEmpty()) sb.append("无");
                    else o.getJoinedEvents().forEach(e ->
                            sb.append(String.format("\n- [%s] %s", e.getTag(), e.getTitle())));
                    return sb.toString();
                })
                .inputType(EmptyInput.class)
                .build();
    }
}
