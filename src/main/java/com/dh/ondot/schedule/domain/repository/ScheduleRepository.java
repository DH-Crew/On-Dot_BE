package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @EntityGraph(attributePaths = {"preparationAlarm", "departureAlarm"})
    Optional<Schedule> findFirstByMemberIdOrderByUpdatedAtDesc(Long memberId);
    
    void deleteByMemberId(Long memberId);
}
