package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import com.dh.ondot.schedule.infra.ScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ScheduleQueryService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryRepository scheduleQueryRepository;

    public Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundScheduleException(id));
    }

    public Schedule findScheduleByIdEager(Long scheduleId) {
        return scheduleQueryRepository.findScheduleById(scheduleId)
                .orElseThrow(() -> new NotFoundScheduleException(scheduleId));
    }

    public Schedule findScheduleByMemberIdAndId(Long memberId, Long scheduleId) {
        return scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId)
                .orElseThrow(() -> new NotFoundScheduleException(scheduleId));
    }

    public Slice<Schedule> getActiveSchedules(Long memberId, Pageable page) {
        Instant now = DateTimeUtils.nowSeoulInstant();
        return scheduleQueryRepository.findActiveSchedulesByMember(memberId, now, page);
    }
}
