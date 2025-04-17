package com.dh.ondot.schedule.infra;

import com.dh.ondot.schedule.domain.QAlarm;
import com.dh.ondot.schedule.domain.QPlace;
import com.dh.ondot.schedule.domain.QSchedule;
import com.dh.ondot.schedule.domain.Schedule;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ScheduleQueryRepository {
    private final JPAQueryFactory q;

    private static final QSchedule s  = QSchedule.schedule;
    private static final QAlarm pa = new QAlarm("pa");// preparationAlarm
    private static final QAlarm da = new QAlarm("da");// departureAlarm
    private static final QPlace dp = new QPlace("dp");// departurePlace
    private static final QPlace ap = new QPlace("ap");// arrivalPlace

    public Optional<Schedule> findScheduleByMemberIdAndId(Long memberId, Long scheduleId) {
        Schedule result = q.selectFrom(s)
                .join(s.preparationAlarm, pa).fetchJoin()
                .join(s.departureAlarm, da).fetchJoin()
                .join(s.departurePlace, dp).fetchJoin()
                .join(s.arrivalPlace, ap).fetchJoin()
                .where(s.id.eq(scheduleId), s.memberId.eq(memberId))
                .fetchOne();
        return Optional.ofNullable(result);
    }

    public Slice<Schedule> findPageByMember(Long memberId, Pageable pageable) {
        List<Schedule> content = q.selectFrom(s)
                .join(s.preparationAlarm, pa).fetchJoin()
                .join(s.departureAlarm, da).fetchJoin()
                .where(s.memberId.eq(memberId))
                .orderBy(s.nextAlarmAt.asc(), s.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) content.remove(content.size() - 1);

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
