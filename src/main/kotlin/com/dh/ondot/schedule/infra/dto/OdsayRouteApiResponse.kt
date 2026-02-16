package com.dh.ondot.schedule.infra.dto

data class OdsayRouteApiResponse(
    val result: Result?,
) {
    data class Result(
        val searchType: Int,
        val outTrafficCheck: Int,
        val busCount: Int,
        val subwayCount: Int,
        val subwayBusCount: Int,
        val pointDistance: Double,
        val startRadius: Int,
        val endRadius: Int,
        val path: List<Path>?,
    )

    data class Path(
        val pathType: Int,
        val info: Info,
        val subPath: List<SubPath>,
    )

    data class Info(
        val trafficDistance: Double,
        val totalWalk: Int,
        val totalTime: Int,
        val payment: Int,
        val busTransitCount: Int,
        val subwayTransitCount: Int,
        val mapObj: String?,
        val firstStartStation: String?,
        val firstStartStationKor: String?,
        val firstStartStationJpnKata: String?,
        val lastEndStation: String?,
        val lastEndStationKor: String?,
        val lastEndStationJpnKata: String?,
        val totalStationCount: Int,
        val busStationCount: Int,
        val subwayStationCount: Int,
        val totalDistance: Double,
        val totalWalkTime: Int,
        val checkIntervalTime: Int,
        val checkIntervalTimeOverYn: String?,
        val totalIntervalTime: Int,
    )

    data class SubPath(
        val trafficType: Int,
        val distance: Double,
        val sectionTime: Int,
        val stationCount: Int?,
        val lane: List<Lane>?,
        val intervalTime: Int?,
        val startName: String?,
        val startNameKor: String?,
        val startNameJpnKata: String?,
        val startX: Double,
        val startY: Double,
        val endName: String?,
        val endNameKor: String?,
        val endNameJpnKata: String?,
        val endX: Double,
        val endY: Double,
        val way: String?,
        val wayCode: Int?,
        val door: String?,
        val startID: Int?,
        val endID: Int?,
        val startStationCityCode: Int?,
        val startStationProviderCode: Int?,
        val startLocalStationID: String?,
        val startArsID: String?,
        val endStationCityCode: Int?,
        val endStationProviderCode: Int?,
        val endLocalStationID: String?,
        val endArsID: String?,
        val startExitNo: String?,
        val startExitX: Double?,
        val startExitY: Double?,
        val endExitNo: String?,
        val endExitX: Double?,
        val endExitY: Double?,
        val passStopList: PassStopList?,
    )

    data class Lane(
        val name: String?,
        val nameKor: String?,
        val nameJpnKata: String?,
        val subwayCode: Int?,
        val subwayCityCode: Int?,
        val busNo: String?,
        val busNoKor: String?,
        val busNoJpnKata: String?,
        val type: Int?,
        val busID: Int?,
        val busLocalBlID: String?,
        val busCityCode: Int?,
        val busProviderCode: Int?,
    )

    data class PassStopList(val stations: List<Station>?)

    data class Station(
        val index: Int,
        val stationID: Int,
        val stationName: String?,
        val stationNameKor: String?,
        val stationNameJpnKata: String?,
        val stationCityCode: Int?,
        val stationProviderCode: Int?,
        val localStationID: String?,
        val arsID: String?,
        val x: String?,
        val y: String?,
        val isNonStop: String?,
    )

    companion object {
        fun walkOnly(estimatedMinutes: Int): OdsayRouteApiResponse {
            val walkInfo = Info(
                trafficDistance = 0.0,
                totalWalk = 0,
                totalTime = estimatedMinutes,
                payment = 0,
                busTransitCount = 0,
                subwayTransitCount = 0,
                mapObj = null,
                firstStartStation = null,
                firstStartStationKor = null,
                firstStartStationJpnKata = null,
                lastEndStation = null,
                lastEndStationKor = null,
                lastEndStationJpnKata = null,
                totalStationCount = 0,
                busStationCount = 0,
                subwayStationCount = 0,
                totalDistance = 0.0,
                totalWalkTime = estimatedMinutes,
                checkIntervalTime = 0,
                checkIntervalTimeOverYn = null,
                totalIntervalTime = 0,
            )

            val walkPath = Path(
                pathType = 10, // custom code to indicate walk-only
                info = walkInfo,
                subPath = emptyList(),
            )

            val result = Result(
                searchType = 0,
                outTrafficCheck = 0,
                busCount = 0,
                subwayCount = 0,
                subwayBusCount = 0,
                pointDistance = 0.0,
                startRadius = 0,
                endRadius = 0,
                path = listOf(walkPath),
            )

            return OdsayRouteApiResponse(result)
        }
    }
}
