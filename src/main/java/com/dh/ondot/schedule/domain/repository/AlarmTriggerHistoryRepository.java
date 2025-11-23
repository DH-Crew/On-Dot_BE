package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.AlarmTriggerHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmTriggerHistoryRepository extends JpaRepository<AlarmTriggerHistory, Long> {
}
