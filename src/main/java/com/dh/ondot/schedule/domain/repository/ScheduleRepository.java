package com.dh.ondot.schedule.domain.repository;

import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    @Query(nativeQuery = true, value = """
        select *
        from schedules s
        join alarms p on s.preparation_alarm_id = p.alarm_id
        join alarms d on s.departure_alarm_id = d.alarm_id
        where s.member_id = :memberId
        order by s.updated_at desc
        limit 1
    """)
    Optional<Schedule> findLatestScheduleByMemberId(@Param("memberId") Long memberId);
}
