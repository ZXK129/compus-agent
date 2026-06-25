package com.starrycampus.library.repository;

import com.starrycampus.library.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findBySeatCode(String seatCode);
    List<Seat> findAllByOrderBySeatCodeAsc();

    /** 按楼层区域查询所有座位 */
    List<Seat> findByFloorAreaOrderBySeatCodeAsc(String floorArea);

    /** 查询某学生当前占用的座位（一人一座校验） */
    Optional<Seat> findByStudentIdAndStatus(Long studentId, String status);

    /** 统计某学生当前占用座位数 */
    long countByStudentIdAndStatus(Long studentId, String status);
}
