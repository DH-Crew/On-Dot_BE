package com.dh.ondot.schedule.infra

import com.dh.ondot.schedule.domain.QAlarm
import com.dh.ondot.schedule.domain.QPlace
import com.dh.ondot.schedule.domain.QSchedule
import com.dh.ondot.schedule.domain.Schedule
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class CalendarQueryRepository(
    private val q: JPAQueryFactory,
) {
    companion object {
        private val s = QSchedule.schedule
        private val pa = QAlarm("calPa")
        private val da = QAlarm("calDa")
        private val dp = QPlace("calDp")
        private val ap = QPlace("calAp")
    }

    /**
     * 캘린더 범위 조회용: 범위 내 표시될 수 있는 모든 스케줄을 가져온다.
     * - 비반복 + deletedAt IS NULL: appointmentAt이 범위 내
     * - 비반복 + deletedAt IS NOT NULL: appointmentAt이 범위 내 AND appointmentAt < deletedAt (삭제 전 기록)
     * - 반복 + deletedAt IS NULL: createdAt <= 범위 끝 (날짜별 매칭은 앱 레이어에서)
     * - 반복 + deletedAt IS NOT NULL: createdAt <= 범위 끝 (삭제된 반복일정의 과거 기록)
     */
    fun findSchedulesForCalendarRange(
        memberId: Long, rangeStart: Instant, rangeEnd: Instant,
    ): List<Schedule> =
        q.selectFrom(s)
            .leftJoin(s.preparationAlarm, pa).fetchJoin()
            .leftJoin(s.departureAlarm, da).fetchJoin()
            .leftJoin(s.departurePlace, dp).fetchJoin()
            .leftJoin(s.arrivalPlace, ap).fetchJoin()
            .where(
                s.memberId.eq(memberId),
                s.isRepeat.isTrue.and(s.createdAt.loe(rangeEnd))
                    .or(
                        s.isRepeat.isFalse
                            .and(s.appointmentAt.goe(rangeStart))
                            .and(s.appointmentAt.lt(rangeEnd))
                            .and(s.deletedAt.isNull.or(s.deletedAt.gt(s.appointmentAt)))
                    )
            )
            .orderBy(s.appointmentAt.asc())
            .fetch()
}
