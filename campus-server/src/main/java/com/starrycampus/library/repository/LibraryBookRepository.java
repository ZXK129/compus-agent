package com.starrycampus.library.repository;

import com.starrycampus.library.entity.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    List<LibraryBook> findByStudentId(Long studentId);

    /** 统计某学生当前借阅数量（用于限量校验） */
    long countByStudentId(Long studentId);

    /** 查询某学生已过期的图书 */
    List<LibraryBook> findByStudentIdAndDueDateBefore(Long studentId, java.time.LocalDate today);
}
