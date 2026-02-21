package com.dh.ondot.member.domain

import com.dh.ondot.member.domain.enums.OauthProvider
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class OauthInfo(
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    val oauthProvider: OauthProvider,

    @Column(name = "oauth_provider_id", nullable = false)
    val oauthProviderId: String,
) {
    companion object {
        fun of(oauthProvider: OauthProvider, oauthProviderId: String): OauthInfo =
            OauthInfo(oauthProvider, oauthProviderId)
    }
}
