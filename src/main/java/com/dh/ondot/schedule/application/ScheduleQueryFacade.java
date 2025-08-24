package com.dh.ondot.schedule.application;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.notification.domain.service.EmergencyAlertService;
import com.dh.ondot.schedule.api.response.*;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.application.mapper.HomeScheduleListItemMapper;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.service.ScheduleQueryService;
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
        List<HomeScheduleListItem> scheduleItems = homeScheduleListItemMapper.toListOrderedByNextAlarmAt(scheduleSlice.getContent());
        LocalDateTime earliestActiveAlarmTime = findEarliestActiveAlarmTime(scheduleItems);

        return HomeScheduleListResponse.of(earliestActiveAlarmTime, scheduleItems, scheduleSlice.hasNext());
    }

    private LocalDateTime findEarliestActiveAlarmTime(List<HomeScheduleListItem> scheduleItems) {
        LocalDateTime now = DateTimeUtils.nowSeoulDateTime();
        
        return scheduleItems.stream()
                .filter(HomeScheduleListItem::hasActiveAlarm)  // 활성 알람이 있는 일정만 필터링
                .map(HomeScheduleListItem::nextAlarmAt)
                .filter(alarmTime -> alarmTime.isAfter(now))  // 현재 시간 이후의 알람만 고려
                .findFirst()  // 가장 빠른 시간 선택
                .orElse(null);
    }

    public String getIssues(Long scheduleId) {
        Schedule schedule = scheduleQueryService.findScheduleByIdEager(scheduleId);
        String roadAddress = schedule.getArrivalPlace().getRoadAddress();
        // todo: 출발지 기반 긴급 알림, 지하철 알림 추가
        return emergencyAlertService.getIssuesByAddress(roadAddress);
    }
}
