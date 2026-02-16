package com.dh.ondot.member.core

import com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOCIAL_LOGIN
import com.dh.ondot.core.exception.UnsupportedException
import com.dh.ondot.member.domain.enums.OauthProvider
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class OauthProviderConverter : Converter<String, OauthProvider> {
    override fun convert(type: String): OauthProvider =
        try {
            OauthProvider.valueOf(type.uppercase(Locale.ENGLISH))
        } catch (e: IllegalArgumentException) {
            throw UnsupportedException(UNSUPPORTED_SOCIAL_LOGIN, type)
        }
}
