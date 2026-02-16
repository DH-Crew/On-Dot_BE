package com.dh.ondot.schedule.fixture

import com.dh.ondot.schedule.domain.Place

object PlaceFixture {

    @JvmStatic
    fun defaultDeparturePlace(): Place = Place(
        0L,
        "집",
        "서울특별시 강남구 테헤란로 1길",
        126.9780,
        37.5665
    )

    @JvmStatic
    fun defaultArrivalPlace(): Place = Place(
        0L,
        "회사",
        "서울특별시 강남구 테헤란로 2길",
        127.0000,
        37.5000
    )
}
