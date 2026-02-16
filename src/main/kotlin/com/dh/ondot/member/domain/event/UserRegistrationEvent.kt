package com.dh.ondot.member.domain.event

import com.dh.ondot.member.domain.enums.OauthProvider

data class UserRegistrationEvent(
    val memberId: Long,
    val email: String?,
    val oauthProvider: OauthProvider,
    val totalMemberCount: Long,
    val mobileType: String?,
)
