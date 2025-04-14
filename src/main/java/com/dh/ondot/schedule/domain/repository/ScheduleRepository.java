package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
