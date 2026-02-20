package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.domain.enums.TransportType

interface RouteTimeCalculator {
    fun supports(transportType: TransportType): Boolean
    fun calculateRouteTime(startX: Double, startY: Double, endX: Double, endY: Double): Int
}
