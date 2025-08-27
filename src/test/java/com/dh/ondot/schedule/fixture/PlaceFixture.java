package com.dh.ondot.schedule.fixture;

import com.dh.ondot.schedule.domain.Place;

public class PlaceFixture {

    public static Place defaultDeparturePlace() {
        return Place.builder()
                .title("집")
                .roadAddress("서울특별시 강남구 테헤란로 1길")
                .latitude(37.5665)
                .longitude(126.9780)
                .build();
    }

    public static Place defaultArrivalPlace() {
        return Place.builder()
                .title("회사")
                .roadAddress("서울특별시 강남구 테헤란로 2길")
                .latitude(37.5000)
                .longitude(127.0000)
                .build();
    }
}
