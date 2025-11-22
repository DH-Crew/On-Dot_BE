package com.dh.ondot.schedule.fixture;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;

public class MemberFixture {

    public static Member defaultMember() {
        return Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("테스트유저")
                .preparationTime(30)
                .defaultAlarmMode(AlarmMode.SOUND)
                .snooze(Snooze.of(true, 5, 3))
                .sound(Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5))
                .build();
    }
}
