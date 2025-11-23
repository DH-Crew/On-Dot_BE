package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.core.exception.NotFoundAlarmException;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.AlarmTriggerHistory;
import com.dh.ondot.schedule.domain.repository.AlarmRepository;
import com.dh.ondot.schedule.domain.repository.AlarmTriggerHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmTriggerHistoryRepository alarmTriggerHistoryRepository;
    private final AlarmRepository alarmRepository;

    @Transactional
    public void recordTrigger(
            Long alarmId,
            Long scheduleId,
            String action,
            String deviceType
    ) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new NotFoundAlarmException(alarmId));

        AlarmTriggerHistory history = AlarmTriggerHistory.record(
                alarmId,
                scheduleId,
                alarm.getTriggeredAt(),
                action,
                deviceType
        );

        alarmTriggerHistoryRepository.save(history);
    }
}
