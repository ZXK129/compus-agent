package com.starrycampus.presence.service.impl;

import com.starrycampus.library.entity.Seat;
import com.starrycampus.library.repository.SeatRepository;
import com.starrycampus.presence.dto.CampusMomentDTO;
import com.starrycampus.presence.entity.CampusMoment;
import com.starrycampus.presence.repository.CampusMomentRepository;
import com.starrycampus.presence.service.PresenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresenceServiceImpl implements PresenceService {

    private final CampusMomentRepository momentRepository;
    private final SeatRepository seatRepository;

    public PresenceServiceImpl(CampusMomentRepository momentRepository, SeatRepository seatRepository) {
        this.momentRepository = momentRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    public List<CampusMomentDTO> getMoments() {
        return momentRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CampusMomentDTO createMoment(Long studentId, String title, String tag, String location) {
        CampusMoment moment = CampusMoment.builder()
                .studentId(studentId).title(title).tag(tag).location(location)
                .likes(0).joined((byte) 0).currentAttendees(0).createdAt(LocalDateTime.now()).build();
        return toDTO(momentRepository.save(moment));
    }

    @Override
    @Transactional
    public CampusMomentDTO joinEvent(Long momentId, Long studentId) {
        CampusMoment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("活动不存在: " + momentId));
        if (moment.getJoined() == (byte) 1) return toDTO(moment);
        moment.setJoined((byte) 1);
        if (moment.getCurrentAttendees() != null) moment.setCurrentAttendees(moment.getCurrentAttendees() + 1);
        return toDTO(momentRepository.save(moment));
    }

    @Override
    public String getCheckinStatus(Long studentId) {
        var occupied = seatRepository.findByStudentIdAndStatus(studentId, "occupied");
        if (occupied.isPresent()) {
            Seat seat = occupied.get();
            return String.format("📍 当前位置：图书馆 %s 号座舱 | 状态：专心修仙中 ✍️", seat.getSeatCode());
        }
        return "📍 当前位置：未签到 | 请在左侧面板入座签到";
    }

    @Override
    @Transactional
    public String checkin(Long studentId) {
        var existing = seatRepository.findByStudentIdAndStatus(studentId, "occupied");
        if (existing.isPresent()) {
            return String.format("📍 已在 %s 号座舱签到，无需重复签到", existing.get().getSeatCode());
        }
        List<com.starrycampus.library.entity.Seat> allSeats = seatRepository.findAllByOrderBySeatCodeAsc();
        Seat target = allSeats.stream()
                .filter(s -> "available".equals(s.getStatus())).findFirst()
                .orElseThrow(() -> new RuntimeException("当前无可用自习座位"));
        target.setStatus("occupied");
        target.setStudentId(studentId);
        target.setBookedStart(LocalDateTime.now());
        target.setBookedEnd(LocalDateTime.now().plusHours(4));
        seatRepository.save(target);
        return String.format("📍 **地理围栏签到成功**\n座位：图书馆 %s 号舱 ✅", target.getSeatCode());
    }

    private CampusMomentDTO toDTO(CampusMoment m) {
        return CampusMomentDTO.builder()
                .id(m.getId()).title(m.getTitle()).tag(m.getTag()).location(m.getLocation())
                .likes(m.getLikes()).joined(m.getJoined())
                .maxAttendees(m.getMaxAttendees()).currentAttendees(m.getCurrentAttendees())
                .timeLabel(formatTimeAgo(m.getCreatedAt())).createdAt(m.getCreatedAt()).build();
    }

    private String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "";
        long minutes = Duration.between(time, LocalDateTime.now()).toMinutes();
        if (minutes < 60) return minutes + "分钟前";
        if (minutes < 1440) return (minutes / 60) + "小时前";
        return (minutes / 1440) + "天前";
    }
}
