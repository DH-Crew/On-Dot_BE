package com.dh.ondot.notification.infra.discord

import com.dh.ondot.member.domain.enums.OauthProvider
import org.springframework.stereotype.Component

@Component
class DiscordMessageTemplate {

    fun createUserRegistrationMessage(
        memberEmail: String?,
        oauthProvider: OauthProvider,
        totalMemberCount: Long,
        mobileType: String?,
    ): String {
        return String.format(
            """
                    |🎉 **온닷 %,d번째 신규 사용자 가입 완료!** 🎉
                    |
                    |👤 **사용자 계정**: %s
                    |🔐 **가입 방식**: %s
                    |📱 **디바이스 타입**: %s
                    |👥 **총 사용자 수**: %,d명
            """.trimMargin(),
            totalMemberCount,
            sanitizeEmail(memberEmail),
            getOauthProviderDisplayName(oauthProvider),
            getMobileTypeDisplayName(mobileType),
            totalMemberCount,
        )
    }

    private fun sanitizeEmail(email: String?): String? {
        if (email.isNullOrEmpty()) {
            return email
        }
        val atIndex = email.indexOf('@')
        return if (atIndex > 0) email.substring(0, atIndex) else email
    }

    private fun getOauthProviderDisplayName(provider: OauthProvider): String {
        return when (provider) {
            OauthProvider.KAKAO -> "Kakao"
            OauthProvider.APPLE -> "Apple"
        }
    }

    private fun getMobileTypeDisplayName(mobileType: String?): String {
        if (mobileType.isNullOrBlank()) {
            return "값 없음"
        }
        return mobileType.uppercase()
    }
}
