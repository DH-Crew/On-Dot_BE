package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public Schedule findScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundScheduleException(id));
    }

    @Transactional
    public void deleteSchedule(Schedule schedule) {
        scheduleRepository.delete(schedule);
    }
}
