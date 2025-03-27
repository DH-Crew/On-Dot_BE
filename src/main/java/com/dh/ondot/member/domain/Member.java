package com.dh.ondot.member.domain;

import com.dh.ondot.core.AggregateRoot;
import com.dh.ondot.core.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@AggregateRoot
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static Member create(String email, String oauthProvider, String oauthProviderId) {
        return Member.builder()
                .email(email)
                .oauthInfo(OauthInfo.of(oauthProvider, oauthProviderId))
                .build();
    }
}
