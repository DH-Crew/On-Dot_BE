package com.dh.ondot.member.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.member.domain.enums.MapProvider;
import com.dh.ondot.member.domain.enums.OauthProvider;
import com.dh.ondot.schedule.domain.enums.AlarmMode;
import com.dh.ondot.schedule.domain.vo.Snooze;
import com.dh.ondot.schedule.domain.vo.Sound;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@AggregateRoot
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "members")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Embedded
    private OauthInfo oauthInfo;

    @Column(name = "latest_preparation_alarm_id")
    private Long latestPreparationAlarmId;

    @Column(name = "latest_departure_alarm_id")
    private Long latestDepartureAlarmId;

    @Column(name = "preparation_time")
    private Integer preparationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_alarm_mode")
    private AlarmMode defaultAlarmMode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "isSnoozeEnabled", column = @Column(name = "default_is_snooze_enabled")),
            @AttributeOverride(name = "snoozeInterval", column = @Column(name = "default_snooze_interval")),
            @AttributeOverride(name = "snoozeCount", column = @Column(name = "default_snooze_count"))
    })
    private Snooze snooze;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "soundCategory", column = @Column(name = "default_sound_category")),
            @AttributeOverride(name = "ringTone", column = @Column(name = "default_ring_tone")),
            @AttributeOverride(name = "volume", column = @Column(name = "default_volume"))
    })
    private Sound sound;

    @Enumerated(EnumType.STRING)
    @Column(name = "map_provider")
    private MapProvider mapProvider;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public static Member registerWithOauth(String email, OauthProvider oauthProvider, String oauthProviderId) {
        return Member.builder()
                .email(email)
                .oauthInfo(OauthInfo.of(oauthProvider, oauthProviderId))
                .build();
    }

    public void updateOnboarding(
            Integer preparationTime, String alarmMode,
            boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount,
            String soundCategory, String ringTone, Double volume
    ) {
        this.preparationTime = preparationTime;
        this.defaultAlarmMode = AlarmMode.from(alarmMode);
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount);
        this.sound = Sound.of(soundCategory, ringTone, volume);
    }

    public void updateMapProvider(String mapProvider) {
        this.mapProvider = MapProvider.from(mapProvider);
    }

    public boolean checkOnboardingCompleted() {
        return preparationTime != null;
    }

    public void deactivate() {
        this.email = "deactivated-" + this.id + "@ondot.com";
        this.oauthInfo = OauthInfo.of(this.getOauthInfo().getOauthProvider(), "0");
        this.deletedAt = DateTimeUtils.nowSeoulInstant();
    }
}
