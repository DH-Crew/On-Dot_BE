package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query("""
        select s
        from Schedule s
        join fetch s.preparationAlarm
        join fetch s.departureAlarm
        where s.memberId = :memberId
        order by s.updatedAt desc
    """)
    Optional<Schedule> findLatestByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}
