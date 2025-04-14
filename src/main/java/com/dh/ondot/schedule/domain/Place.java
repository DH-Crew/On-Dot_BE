package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "places")
public class Place extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    public static Place createPlace(String title, String roadAddress, Double longitude, Double latitude) {
        return Place.builder()
                .title(title)
                .roadAddress(roadAddress)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }

    public void update(String title, String roadAddress,
                       Double longitude, Double latitude
    ) {
        this.title = title;
        this.roadAddress = roadAddress;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public boolean isPlaceChanged(String roadAddress, Double longitude, Double latitude) {
        return !this.roadAddress.equals(roadAddress)
                || !this.longitude.equals(longitude)
                || !this.latitude.equals(latitude);
    }
}
