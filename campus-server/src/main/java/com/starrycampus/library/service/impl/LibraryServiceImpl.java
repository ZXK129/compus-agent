package com.starrycampus.library.service.impl;

import com.starrycampus.library.dto.LibraryBookDTO;
import com.starrycampus.library.dto.SeatDTO;
import com.starrycampus.library.entity.LibraryBook;
import com.starrycampus.library.entity.Seat;
import com.starrycampus.library.repository.LibraryBookRepository;
import com.starrycampus.library.repository.SeatRepository;
import com.starrycampus.library.service.LibraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryServiceImpl implements LibraryService {

    private static final Logger log = LoggerFactory.getLogger(LibraryServiceImpl.class);

    /** 每位学生最大借书数量 */
    private static final int MAX_BORROW_LIMIT = 5;

    /** 座位预约开放时间 */
    private static final LocalTime BOOKING_OPEN_TIME = LocalTime.of(8, 0);

    /** 座位预约关闭时间 */
    private static final LocalTime BOOKING_CLOSE_TIME = LocalTime.of(21, 0);

    /** 单次预约时长（小时） */
    private static final int BOOKING_DURATION_HOURS = 4;

    /** 借阅默认天数 */
    private static final int DEFAULT_BORROW_DAYS = 30;

    private final LibraryBookRepository bookRepository;
    private final SeatRepository seatRepository;

    public LibraryServiceImpl(LibraryBookRepository bookRepository, SeatRepository seatRepository) {
        this.bookRepository = bookRepository;
        this.seatRepository = seatRepository;
    }

    // ========================================================================
    //  图书相关
    // ========================================================================

    @Override
    public List<LibraryBookDTO> getBooks(Long studentId) {
        return bookRepository.findByStudentId(studentId).stream()
                .map(this::toBookDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LibraryBookDTO renewBook(Long bookId, Long studentId) {
        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在: " + bookId));
        if (!book.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此图书，该书不属于当前用户");
        }
        if (book.getRenewed() == 1) {
            throw new RuntimeException("该书已续借过一次，不可再次续借");
        }
        book.setDueDate(book.getDueDate().plusDays(7));
        book.setRenewed((byte) 1);
        LibraryBook saved = bookRepository.save(book);
        log.info("学生 {} 续借图书《{}》成功，新归还日期: {}", studentId, saved.getTitle(), saved.getDueDate());
        return toBookDTO(saved);
    }

    @Override
    @Transactional
    public LibraryBookDTO extendBook(Long bookId, Long studentId, int days) {
        if (days <= 0) {
            throw new RuntimeException("延期天数必须大于0天");
        }
        if (days > 90) {
            throw new RuntimeException("单次最多延期90天");
        }
        LibraryBook book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在: " + bookId));
        if (!book.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此图书，该书不属于当前用户");
        }
        // 延期：不限制次数，每次延长指定天数
        book.setDueDate(book.getDueDate().plusDays(days));
        book.setRenewed((byte) (book.getRenewed() + 1)); // 累计延期次数
        LibraryBook saved = bookRepository.save(book);
        log.info("学生 {} 延期图书《{}》{}天，新归还日期: {}（累计延期{}次）",
                studentId, saved.getTitle(), days, saved.getDueDate(), saved.getRenewed());
        return toBookDTO(saved);
    }

    @Override
    @Transactional
    public LibraryBookDTO borrowBook(String isbn, String title, String author, Long studentId) {
        // 1. 检查借阅数量上限
        long currentCount = bookRepository.countByStudentId(studentId);
        if (currentCount >= MAX_BORROW_LIMIT) {
            throw new RuntimeException(String.format(
                    "借阅已达上限（%d/%d 本），请先归还部分图书后再借", currentCount, MAX_BORROW_LIMIT));
        }

        // 2. 检查是否有逾期未还的图书
        List<LibraryBook> overdueBooks = bookRepository
                .findByStudentIdAndDueDateBefore(studentId, LocalDate.now());
        if (!overdueBooks.isEmpty()) {
            String titles = overdueBooks.stream()
                    .map(LibraryBook::getTitle)
                    .collect(Collectors.joining("》、《"));
            throw new RuntimeException(String.format(
                    "您有 %d 本逾期图书未归还（《%s》），请先归还后再借阅新书", overdueBooks.size(), titles));
        }

        // 3. 借阅新书
        LibraryBook book = LibraryBook.builder()
                .studentId(studentId)
                .isbn(isbn)
                .title(title)
                .author(author)
                .coverColor("bg-neutral-900")
                .dueDate(LocalDate.now().plusDays(DEFAULT_BORROW_DAYS))
                .progress(0)
                .renewed((byte) 0)
                .build();
        LibraryBook saved = bookRepository.save(book);
        log.info("学生 {} 借阅《{}》成功，归还日期: {}, 当前借阅 {}/{}",
                studentId, saved.getTitle(), saved.getDueDate(), currentCount + 1, MAX_BORROW_LIMIT);
        return toBookDTO(saved);
    }

    @Override
    public int getMaxBorrowLimit() {
        return MAX_BORROW_LIMIT;
    }

    @Override
    public String searchBooks(String keyword) {
        return String.format("🔍 已在「星空大学馆藏库」检索到与「%s」相关的 3 册馆藏。\n"
                + "1. 《生成式AI与Agent前沿》- 3F 科技区\n"
                + "2. 《深度学习理论与实践》- 3F 科技区\n"
                + "3. 《校园数字化建设白皮书》- 2F 社科区", keyword);
    }

    // ========================================================================
    //  座位相关
    // ========================================================================

    @Override
    public List<SeatDTO> getSeats() {
        return seatRepository.findAllByOrderBySeatCodeAsc().stream()
                .map(this::toSeatDTO).collect(Collectors.toList());
    }

    @Override
    public List<SeatDTO> getSeatsByFloor(String floorArea) {
        return seatRepository.findByFloorAreaOrderBySeatCodeAsc(floorArea).stream()
                .map(this::toSeatDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatDTO bookSeat(Long seatId, Long studentId) {
        // 1. 校验预约时间：仅限 8:00 - 21:00
        LocalTime now = LocalTime.now();
        if (now.isBefore(BOOKING_OPEN_TIME) || now.isAfter(BOOKING_CLOSE_TIME)) {
            throw new RuntimeException(String.format(
                    "预约时间为每日 %s - %s，当前时间 %s 不在预约时段内",
                    BOOKING_OPEN_TIME.format(DateTimeFormatter.ofPattern("HH:mm")),
                    BOOKING_CLOSE_TIME.format(DateTimeFormatter.ofPattern("HH:mm")),
                    now.format(DateTimeFormatter.ofPattern("HH:mm"))));
        }

        // 2. 查询目标座位
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("座位不存在: " + seatId));

        // 3. 防止一座多人预约：已占用则拒绝
        if ("occupied".equals(seat.getStatus())) {
            throw new RuntimeException(String.format(
                    "座位 %s 已被其他同学预约，请选择其他空闲座位", seat.getSeatCode()));
        }

        // 4. 一人一座：释放当前已占座位
        seatRepository.findByStudentIdAndStatus(studentId, "occupied").ifPresent(current -> {
            log.info("学生 {} 更换座位: {} → {}", studentId, current.getSeatCode(), seat.getSeatCode());
            current.setStatus("available");
            current.setStudentId(null);
            current.setBookedStart(null);
            current.setBookedEnd(null);
            seatRepository.save(current);
        });

        // 5. 预约新座位
        seat.setStatus("occupied");
        seat.setStudentId(studentId);
        seat.setBookedStart(LocalDateTime.now());
        seat.setBookedEnd(LocalDateTime.now().plusHours(BOOKING_DURATION_HOURS));
        Seat saved = seatRepository.save(seat);
        log.info("学生 {} 预约座位 {} ({}F) 成功，时段: {} - {}",
                studentId, saved.getSeatCode(),
                saved.getFloorArea(),
                saved.getBookedStart().format(DateTimeFormatter.ofPattern("HH:mm")),
                saved.getBookedEnd().format(DateTimeFormatter.ofPattern("HH:mm")));
        return toSeatDTO(saved);
    }

    @Override
    @Transactional
    public void releaseSeat(Long seatId, Long studentId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("座位不存在: " + seatId));
        if (!studentId.equals(seat.getStudentId())) {
            throw new RuntimeException("无权释放此座位，该座位由其他同学预约");
        }
        if (!"occupied".equals(seat.getStatus())) {
            throw new RuntimeException("该座位当前为空闲状态，无需释放");
        }
        seat.setStatus("available");
        seat.setStudentId(null);
        seat.setBookedStart(null);
        seat.setBookedEnd(null);
        seatRepository.save(seat);
        log.info("学生 {} 释放座位 {} ({}F)", studentId, seat.getSeatCode(), seat.getFloorArea());
    }

    @Override
    public String getCurrentBookedSeatCode(Long studentId) {
        return seatRepository.findByStudentIdAndStatus(studentId, "occupied")
                .map(Seat::getSeatCode)
                .orElse(null);
    }

    // ========================================================================
    //  DTO 转换
    // ========================================================================

    private LibraryBookDTO toBookDTO(LibraryBook book) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(), book.getDueDate());
        return LibraryBookDTO.builder()
                .id(book.getId()).title(book.getTitle()).author(book.getAuthor())
                .isbn(book.getIsbn()).coverColor(book.getCoverColor())
                .dueDate(book.getDueDate()).daysRemaining((int) Math.max(0, days))
                .progress(book.getProgress()).renewed(book.getRenewed()).build();
    }

    private SeatDTO toSeatDTO(Seat seat) {
        String timeLabel = "";
        if (seat.getBookedStart() != null && seat.getBookedEnd() != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            timeLabel = "今日 " + seat.getBookedStart().format(fmt)
                    + " - " + seat.getBookedEnd().format(fmt);
        }
        return SeatDTO.builder()
                .id(seat.getId()).seatCode(seat.getSeatCode())
                .floorArea(seat.getFloorArea())
                .status(seat.getStatus())
                .statusLabel("occupied".equals(seat.getStatus()) ? "占用" : "空闲")
                .bookedStart(seat.getBookedStart()).bookedEnd(seat.getBookedEnd())
                .timeLabel(timeLabel).build();
    }
}
