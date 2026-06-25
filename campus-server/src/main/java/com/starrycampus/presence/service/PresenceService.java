package com.starrycampus.presence.service;

import com.starrycampus.presence.dto.CampusMomentDTO;
import java.util.List;

public interface PresenceService {
    List<CampusMomentDTO> getMoments();
    CampusMomentDTO createMoment(Long studentId, String title, String tag, String location);
    CampusMomentDTO joinEvent(Long momentId, Long studentId);
    String getCheckinStatus(Long studentId);
    String checkin(Long studentId);
}
