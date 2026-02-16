package com.dh.ondot.member.domain

import com.dh.ondot.member.domain.enums.OauthProvider

interface OauthApiFactory {
    fun getOauthApi(provider: OauthProvider): OauthApi
}
