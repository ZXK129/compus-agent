package com.starrycampus.library.controller;

import com.starrycampus.common.base.ApiResponse;
import com.starrycampus.library.dto.BorrowRequest;
import com.starrycampus.library.dto.LibraryBookDTO;
import com.starrycampus.library.dto.SearchRequest;
import com.starrycampus.library.dto.SeatDTO;
import com.starrycampus.library.service.LibraryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private static final Logger log = LoggerFactory.getLogger(LibraryController.class);

    private final LibraryService libraryService;

    @Value("${campus.demo-student-id:1}")
    private Long defaultStudentId;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // ===== 图书接口 =====

    @GetMapping("/books")
    public ApiResponse<List<LibraryBookDTO>> getBooks() {
        List<LibraryBookDTO> books = libraryService.getBooks(defaultStudentId);
        return ApiResponse.success(String.format("当前借阅 %d/%d 本",
                books.size(), libraryService.getMaxBorrowLimit()), books);
    }

    @PostMapping("/books/{id}/renew")
    public ApiResponse<LibraryBookDTO> renewBook(@PathVariable Long id) {
        try {
            LibraryBookDTO dto = libraryService.renewBook(id, defaultStudentId);
            return ApiResponse.success("续借成功，归还日期已顺延 7 天", dto);
        } catch (RuntimeException e) {
            log.warn("续借失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/books/{id}/extend")
    public ApiResponse<LibraryBookDTO> extendBook(@PathVariable Long id, @RequestParam(defaultValue = "14") int days) {
        try {
            LibraryBookDTO dto = libraryService.extendBook(id, defaultStudentId, days);
            return ApiResponse.success(String.format("延期成功！+%d天，新归还日期：%s", days, dto.getDueDate()), dto);
        } catch (RuntimeException e) {
            log.warn("延期失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/books/borrow")
    public ApiResponse<LibraryBookDTO> borrowBook(@Valid @RequestBody BorrowRequest request) {
        try {
            LibraryBookDTO dto = libraryService.borrowBook(
                    request.getIsbn(), request.getTitle(), request.getAuthor(), defaultStudentId);
            return ApiResponse.success(String.format("借阅成功！归还日期：%s，当前借阅进度已更新", dto.getDueDate()), dto);
        } catch (RuntimeException e) {
            log.warn("借阅失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/search")
    public ApiResponse<String> searchBooks(@Valid @RequestBody SearchRequest request) {
        return ApiResponse.success(libraryService.searchBooks(request.getKeyword()));
    }

    // ===== 座位接口 =====

    @GetMapping("/seats")
    public ApiResponse<List<SeatDTO>> getSeats(
            @RequestParam(required = false) String floor) {
        List<SeatDTO> seats;
        if (floor != null && !floor.isEmpty()) {
            seats = libraryService.getSeatsByFloor(floor + "F");
        } else {
            seats = libraryService.getSeats();
        }
        String currentSeat = libraryService.getCurrentBookedSeatCode(defaultStudentId);
        String msg = currentSeat != null
                ? "当前已预约座位: " + currentSeat
                : "暂未预约座位，可选空闲座位预约（8:00-21:00）";
        return ApiResponse.success(msg, seats);
    }

    @PostMapping("/seats/{id}/book")
    public ApiResponse<SeatDTO> bookSeat(@PathVariable Long id) {
        try {
            SeatDTO dto = libraryService.bookSeat(id, defaultStudentId);
            return ApiResponse.success(String.format("座位 %s 预约成功！时段：%s",
                    dto.getSeatCode(), dto.getTimeLabel()), dto);
        } catch (RuntimeException e) {
            log.warn("座位预约失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @PostMapping("/seats/{id}/release")
    public ApiResponse<Void> releaseSeat(@PathVariable Long id) {
        try {
            libraryService.releaseSeat(id, defaultStudentId);
            return ApiResponse.success("已释放座位", null);
        } catch (RuntimeException e) {
            log.warn("释放座位失败: {}", e.getMessage());
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
