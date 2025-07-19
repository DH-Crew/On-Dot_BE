package com.dh.ondot.schedule.application;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.notification.domain.service.EmergencyAlertService;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.infra.ScheduleQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryFacade {
    private final MemberService memberService;
    private final EmergencyAlertService emergencyAlertService;
    private final ScheduleQueryRepository scheduleQueryRepository;

    public Schedule findOne(Long memberId, Long scheduleId) {
        memberService.findExistingMember(memberId);

        return scheduleQueryRepository.findScheduleByMemberIdAndId(memberId, scheduleId)
                .orElseThrow(() -> new NotFoundScheduleException(scheduleId));
    }

    public HomeScheduleListResponse findAll(Long memberId, Pageable page) {
        memberService.findExistingMember(memberId);
        Slice<Schedule> slice =  scheduleQueryRepository.findPageByMember(memberId, page);

        // Refresh nextAlarmAt and filter out expired one-time schedules
        List<Schedule> filteredSchedules = slice.getContent().stream()
                .filter(schedule -> schedule.isScheduleRepeated() || !schedule.isPastAppointment())
                .peek(Schedule::updateNextAlarmAt)
                .toList();

        // todo: need delete logic for expired schedules

        List<HomeScheduleListItem> homeScheduleListItem = filteredSchedules.stream()
                .map(HomeScheduleListItem::from)
                .sorted(Comparator.comparing(HomeScheduleListItem::nextAlarmAt))
                .toList();

        LocalDateTime now = DateTimeUtils.nowSeoulDateTime();
        LocalDateTime earliest = homeScheduleListItem.stream()
                .filter(HomeScheduleListItem::isEnabled)
                .map(HomeScheduleListItem::nextAlarmAt)
                .filter(next -> next.isAfter(now))
                .findFirst()
                .orElse(null);

        return HomeScheduleListResponse.of(earliest, homeScheduleListItem, slice.hasNext());
    }

    @Transactional(readOnly = true)
    public String getIssues(Long scheduleId) {
        Schedule schedule = scheduleQueryRepository.findScheduleById(scheduleId)
                .orElseThrow(() -> new NotFoundScheduleException(scheduleId));
        String roadAddress = schedule.getArrivalPlace().getRoadAddress();
        // todo: 지하철 알림 추가
        return emergencyAlertService.getIssuesByAddress(roadAddress);
    }
}
