package com.dh.ondot.schedule.domain.repository

import com.dh.ondot.schedule.domain.AlarmTriggerHistory
import org.springframework.data.jpa.repository.JpaRepository

interface AlarmTriggerHistoryRepository : JpaRepository<AlarmTriggerHistory, Long>
