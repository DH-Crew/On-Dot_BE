package com.dh.ondot.member.domain.enums

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_MAP_PROVIDER
import com.dh.ondot.core.exception.UnsupportedException

enum class MapProvider {
    KAKAO,
    NAVER,
    APPLE,
    ;

    companion object {
        fun from(mapProvider: String): MapProvider =
            entries.find { it.name.equals(mapProvider, ignoreCase = true) }
                ?: throw UnsupportedException(UNSUPPORTED_MAP_PROVIDER, mapProvider)
    }
}
