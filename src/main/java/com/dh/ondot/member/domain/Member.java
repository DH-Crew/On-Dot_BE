package com.dh.ondot.member.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.schedule.domain.Sound;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 50)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Embedded
    private OauthInfo oauthInfo;

    private Long latestPreparationAlarmId;

    private Long latestDepartureAlarmId;

    private Integer preparationTime;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ringTone", column = @Column(name = "default_ring_tone")),
            @AttributeOverride(name = "volume", column = @Column(name = "default_volume"))
    })
    private Sound sound;

    @Enumerated(EnumType.STRING)
    private MapProvider mapProvider;

    public static Member registerWithOauth(String email, OauthProvider oauthProvider, String oauthProviderId) {
        return Member.builder()
                .email(email)
                .oauthInfo(OauthInfo.of(oauthProvider, oauthProviderId))
                .build();
    }

    public void updateOnboarding(Integer preparationTime, String ringTone, Integer volume) {
        this.preparationTime = preparationTime;
        this.sound = Sound.of(ringTone, volume);
    }
}
