package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.core.exception.NotFoundAlarmException
import com.dh.ondot.schedule.domain.AlarmTriggerHistory
import com.dh.ondot.schedule.domain.repository.AlarmRepository
import com.dh.ondot.schedule.domain.repository.AlarmTriggerHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AlarmService(
    private val alarmTriggerHistoryRepository: AlarmTriggerHistoryRepository,
    private val alarmRepository: AlarmRepository,
) {
    @Transactional
    fun recordTrigger(
        memberId: Long,
        alarmId: Long,
        scheduleId: Long,
        action: String,
        deviceType: String,
    ) {
        val alarm = alarmRepository.findById(alarmId)
            .orElseThrow { NotFoundAlarmException(alarmId) }

        val history = AlarmTriggerHistory.record(
            memberId,
            alarmId,
            scheduleId,
            alarm.triggeredAt,
            action,
            deviceType,
        )

        alarmTriggerHistoryRepository.save(history)
    }
}
