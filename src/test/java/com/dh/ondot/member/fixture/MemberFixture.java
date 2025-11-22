package com.dh.ondot.member.fixture;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.OauthProvider;
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

    public static Member memberWithId(Long id) {
        return Member.builder()
                .id(id)
                .email("test" + id + "@example.com")
                .nickname("테스트유저" + id)
                .preparationTime(30)
                .defaultAlarmMode(AlarmMode.SOUND)
                .snooze(Snooze.of(true, 5, 3))
                .sound(Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5))
                .build();
    }

    public static Member newMember() {
        return Member.registerWithOauth("new@example.com", OauthProvider.KAKAO, "kakao123");
    }

    public static Member onboardedMember() {
        return Member.builder()
                .id(2L)
                .email("onboarded@example.com")
                .nickname("온보딩완료유저")
                .preparationTime(45)
                .defaultAlarmMode(AlarmMode.SOUND)
                .snooze(Snooze.of(true, 5, 3))
                .sound(Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5))
                .build();
    }
}
