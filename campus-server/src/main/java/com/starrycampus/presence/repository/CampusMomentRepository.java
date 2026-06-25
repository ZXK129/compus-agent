package com.starrycampus.presence.repository;

import com.starrycampus.presence.entity.CampusMoment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CampusMomentRepository extends JpaRepository<CampusMoment, Long> {
    List<CampusMoment> findAllByOrderByCreatedAtDesc();

    /** 查询当前用户已报名的活动 */
    List<CampusMoment> findByJoinedOrderByCreatedAtDesc(Byte joined);
}
