package com.dh.ondot.schedule.application;

import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.notification.domain.service.EmergencyAlertService;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.application.mapper.HomeScheduleListItemMapper;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.service.ScheduleQueryService;
import com.dh.ondot.schedule.domain.service.ScheduleService;
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
    private final ScheduleService scheduleService;
    private final ScheduleQueryService scheduleQueryService;
    private final EmergencyAlertService emergencyAlertService;
    private final HomeScheduleListItemMapper homeScheduleListItemMapper;

    public Schedule findOne(Long scheduleId) {
        return scheduleQueryService.findScheduleById(scheduleId);
    }

    public Schedule findOneByMemberAndSchedule(Long memberId, Long scheduleId) {
        memberService.getMemberIfExists(memberId);
        return scheduleQueryService.findScheduleByMemberIdAndId(memberId, scheduleId);
    }

    public HomeScheduleListResponse findAllActiveSchedules(Long memberId, Pageable page) {
        memberService.getMemberIfExists(memberId);

        Slice<Schedule> scheduleSlice = scheduleQueryService.getActiveSchedules(memberId, page);
        List<HomeScheduleListItem> scheduleItems = homeScheduleListItemMapper.toListOrderedByAlarmPriority(scheduleSlice.getContent());
        Instant earliestActiveAlarmAt = scheduleService.getEarliestActiveAlarmAt(scheduleSlice.getContent());
        Long earliestAlarmId = findEarliestActiveAlarmScheduleId(scheduleItems);

        return HomeScheduleListResponse.of(earliestAlarmId, earliestActiveAlarmAt, scheduleItems, scheduleSlice.hasNext());
    }

    private Long findEarliestActiveAlarmScheduleId(List<HomeScheduleListItem> sortedItems) {
        return sortedItems.stream()
                .filter(HomeScheduleListItem::hasActiveAlarm)
                .findFirst()
                .map(HomeScheduleListItem::scheduleId)
                .orElse(null);
    }

    public String getIssues(Long scheduleId) {
        Schedule schedule = scheduleQueryService.findScheduleByIdEager(scheduleId);
        String roadAddress = schedule.getArrivalPlace().getRoadAddress();
        // todo: 출발지 기반 긴급 알림, 지하철 알림 추가
        return emergencyAlertService.getIssuesByAddress(roadAddress);
    }
}
