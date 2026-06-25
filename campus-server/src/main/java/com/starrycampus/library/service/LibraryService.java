package com.starrycampus.library.service;

import com.starrycampus.library.dto.LibraryBookDTO;
import com.starrycampus.library.dto.SeatDTO;
import java.util.List;

public interface LibraryService {
    // ===== 图书相关 =====
    List<LibraryBookDTO> getBooks(Long studentId);
    LibraryBookDTO renewBook(Long bookId, Long studentId);
    LibraryBookDTO extendBook(Long bookId, Long studentId, int days);
    LibraryBookDTO borrowBook(String isbn, String title, String author, Long studentId);
    int getMaxBorrowLimit();
    String searchBooks(String keyword);

    // ===== 座位相关 =====
    List<SeatDTO> getSeats();
    List<SeatDTO> getSeatsByFloor(String floorArea);
    SeatDTO bookSeat(Long seatId, Long studentId);
    void releaseSeat(Long seatId, Long studentId);
    String getCurrentBookedSeatCode(Long studentId);
}
