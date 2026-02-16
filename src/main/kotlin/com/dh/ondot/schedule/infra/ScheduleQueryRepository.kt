package com.dh.ondot.schedule.infra

import com.dh.ondot.schedule.domain.QAlarm
import com.dh.ondot.schedule.domain.QPlace
import com.dh.ondot.schedule.domain.QSchedule
import com.dh.ondot.schedule.domain.Schedule
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class ScheduleQueryRepository(
    private val q: JPAQueryFactory,
) {
    companion object {
        private val s = QSchedule.schedule
        private val pa = QAlarm("pa") // preparationAlarm
        private val da = QAlarm("da") // departureAlarm
        private val dp = QPlace("dp") // departurePlace
        private val ap = QPlace("ap") // arrivalPlace
    }

    fun findScheduleById(scheduleId: Long): Optional<Schedule> {
        val result = q.selectFrom(s)
            .join(s.departurePlace, dp).fetchJoin()
            .join(s.arrivalPlace, ap).fetchJoin()
            .where(s.id.eq(scheduleId))
            .fetchOne()
        return Optional.ofNullable(result)
    }

    fun findScheduleByMemberIdAndId(memberId: Long, scheduleId: Long): Optional<Schedule> {
        val result = q.selectFrom(s)
            .join(s.preparationAlarm, pa).fetchJoin()
            .join(s.departureAlarm, da).fetchJoin()
            .join(s.departurePlace, dp).fetchJoin()
            .join(s.arrivalPlace, ap).fetchJoin()
            .where(s.id.eq(scheduleId), s.memberId.eq(memberId))
            .fetchOne()
        return Optional.ofNullable(result)
    }

    fun findActiveSchedulesByMember(memberId: Long, now: Instant, pageable: Pageable): Slice<Schedule> {
        val content = q.selectFrom(s)
            .join(s.departurePlace, dp).fetchJoin()
            .join(s.arrivalPlace, ap).fetchJoin()
            .join(s.preparationAlarm, pa).fetchJoin()
            .join(s.departureAlarm, da).fetchJoin()
            .where(
                s.memberId.eq(memberId)
                    .and(s.isRepeat.isTrue.or(s.appointmentAt.goe(now)))
            )
            .orderBy(s.appointmentAt.asc(), s.id.desc())
            .offset(pageable.offset)
            .limit((pageable.pageSize + 1).toLong())
            .fetch()

        val hasNext = content.size > pageable.pageSize
        if (hasNext) {
            content.removeAt(content.size - 1)
        }
        return SliceImpl(content, pageable, hasNext)
    }
}
