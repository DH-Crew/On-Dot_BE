package com.dh.ondot.schedule.app;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.app.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.infra.ScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryFacade {
    private final MemberService memberService;
    private final ScheduleQueryRepository  scheduleQueryRepository;

    public Schedule findOne(Long memberId, Long scheduleId) {
        memberService.findExistingMember(memberId);

        return scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId)
                .orElseThrow(() -> new NotFoundScheduleException(scheduleId));
    }

    public HomeScheduleListResponse findAll(Long memberId, Pageable page) {
        Member member = memberService.findExistingMember(memberId);
        Slice<Schedule> slice =  scheduleQueryRepository.findPageByMember(memberId, page);

        // Refresh nextAlarmAt and filter out expired one-time schedules
        List<Schedule> filteredSchedules = slice.getContent().stream()
                .peek(Schedule::updateNextAlarmAt)
                .filter(schedule -> schedule.isScheduleRepeated() || !schedule.isPastAppointment())
                .toList();

        // todo: need delete logic for expired schedules

        List<HomeScheduleListItem> homeScheduleListItem = filteredSchedules.stream()
                .map(HomeScheduleListItem::from)
                .toList();

        LocalDateTime now = DateTimeUtils.nowSeoulDateTime();
        LocalDateTime earliest = homeScheduleListItem.stream()
                .filter(HomeScheduleListItem::isEnabled)
                .map(HomeScheduleListItem::nextAlarmAt)
                .filter(next -> next.isAfter(now))
                .findFirst()
                .orElse(null);

        boolean isOnboardingCompleted = member.checkOnboardingCompleted();

        return HomeScheduleListResponse.of(isOnboardingCompleted, earliest, homeScheduleListItem, slice.hasNext());
    }
}
