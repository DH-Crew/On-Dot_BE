package com.dh.ondot.member.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "answers")
public class Address extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AddressType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "longtitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    public static Address createByOnboarding(Member member, String title, double longitude, double latitude) {
        return Address.builder()
                .member(member)
                .type(AddressType.HOME)
                .title(title)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }
}
