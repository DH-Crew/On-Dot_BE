package com.dh.ondot.member.domain.enums

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_ADDRESS_TYPE
import com.dh.ondot.core.exception.UnsupportedException

enum class AddressType {
    HOME,
    ;

    companion object {
        @JvmStatic
        fun from(type: String): AddressType =
            entries.find { it.name.equals(type, ignoreCase = true) }
                ?: throw UnsupportedException(UNSUPPORTED_ADDRESS_TYPE, type)
    }
}
