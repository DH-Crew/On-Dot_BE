package com.dh.ondot.member.domain

import com.dh.ondot.member.domain.dto.UserInfo

interface OauthApi {
    fun fetchUser(accessToken: String): UserInfo
}
