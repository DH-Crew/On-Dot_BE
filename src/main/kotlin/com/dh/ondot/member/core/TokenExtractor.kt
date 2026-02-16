package com.dh.ondot.member.core

import com.dh.ondot.member.core.exception.InvalidTokenHeaderException
import com.dh.ondot.member.core.exception.TokenMissingException

object TokenExtractor {
    private const val BEARER_PREFIX = "Bearer "

    @JvmStatic
    fun extract(header: String?): String {
        if (header == null) {
            throw TokenMissingException()
        }
        if (!header.startsWith(BEARER_PREFIX)) {
            throw InvalidTokenHeaderException()
        }
        return header.substring(BEARER_PREFIX.length)
    }
}
