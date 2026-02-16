package com.dh.ondot.member.api.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.Member
import com.dh.ondot.member.domain.enums.MapProvider
import java.time.LocalDateTime

data class MapProviderResponse(
    val mapProvider: MapProvider,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member): MapProviderResponse =
            MapProviderResponse(
                mapProvider = member.mapProvider!!,
                updatedAt = TimeUtils.toSeoulDateTime(member.updatedAt)!!,
            )
    }
}
