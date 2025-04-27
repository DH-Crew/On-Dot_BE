package com.dh.ondot.schedule.app;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import com.dh.ondot.schedule.domain.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlarmFacade {
    private final MemberService memberService;
    private final RouteService routeService;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public Schedule generateAlarmSettingByRoute(
            Long memberId, LocalDateTime appointmentAt,
            Double startX, Double startY, Double endX, Double endY
    ) {
        Member member = memberService.findExistingMember(memberId);

        Integer estimatedTime = routeService.calculateRouteTime(
                startX, startY,
                endX, endY
        );

        Optional<Schedule> schedule = scheduleRepository
                .findFirstByMemberIdOrderByUpdatedAtDesc(memberId);

        if (schedule.isPresent()) {
            Schedule latestSchedule = schedule.get();
            latestSchedule.getPreparationAlarm().updateTriggeredAt(appointmentAt.minusMinutes(estimatedTime + member.getPreparationTime()));
            latestSchedule.getDepartureAlarm().updateTriggeredAt(appointmentAt.minusMinutes(estimatedTime));

            return latestSchedule;
        } else {
            return Schedule.createWithDefaultAlarmSetting(
                    member.getDefaultAlarmMode(), member.getSnooze(), member.getSound(),
                    appointmentAt, estimatedTime, member.getPreparationTime()
            );
        }
    }
}
