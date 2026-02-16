package com.dh.ondot.member.fixture;

import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.OauthInfo;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;

public class MemberFixture {

    public static Member defaultMember() {
        return new Member(
                1L,
                "테스트유저",
                "test@example.com",
                OauthInfo.of(OauthProvider.KAKAO, "kakao123"),
                null,
                null,
                30,
                AlarmMode.SOUND,
                Snooze.of(true, 5, 3),
                Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5),
                null,
                null
        );
    }

    public static Member memberWithId(Long id) {
        return new Member(
                id,
                "테스트유저" + id,
                "test" + id + "@example.com",
                OauthInfo.of(OauthProvider.KAKAO, "kakao" + id),
                null,
                null,
                30,
                AlarmMode.SOUND,
                Snooze.of(true, 5, 3),
                Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5),
                null,
                null
        );
    }

    public static Member newMember() {
        return Member.registerWithOauth("new@example.com", OauthProvider.KAKAO, "kakao123");
    }

    public static Member onboardedMember() {
        return new Member(
                2L,
                "온보딩완료유저",
                "onboarded@example.com",
                OauthInfo.of(OauthProvider.KAKAO, "kakao456"),
                null,
                null,
                45,
                AlarmMode.SOUND,
                Snooze.of(true, 5, 3),
                Sound.of("BRIGHT_ENERGY", "DANCING_IN_THE_STARDUST", 0.5),
                null,
                null
        );
    }
}
