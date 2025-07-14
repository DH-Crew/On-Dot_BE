package com.dh.ondot.member.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.member.domain.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "addresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_type",
                columnNames = {"member_id", "type"}
        )
)
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

    @Column(name = "name")
    private String name;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "longtitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    public static Address createByOnboarding(Member member, String roadAddress, double longitude, double latitude) {
        return Address.builder()
                .member(member)
                .type(AddressType.HOME)
                .roadAddress(roadAddress)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }

    public void update(String roadAddress, double longitude, double latitude) {
        this.roadAddress = roadAddress;
        this.longitude   = longitude;
        this.latitude    = latitude;
    }
}
