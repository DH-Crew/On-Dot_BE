package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
}
