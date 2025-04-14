package com.dh.ondot.schedule.app;

import com.dh.ondot.member.domain.service.MemberService;
import com.dh.ondot.schedule.core.exception.NotFoundScheduleException;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlarmFacade {

    private final MemberService memberService;
    private final ScheduleRepository scheduleRepository;

    @Transactional(readOnly = true)
    public Schedule getLatestAlarms(Long memberId) {
        memberService.findExistingMember(memberId);

        return scheduleRepository
                .findLatestByMemberId(memberId)
                .orElseThrow(() -> new NotFoundScheduleException(memberId));
    }
}
