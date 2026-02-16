package com.dh.ondot.member.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserInfoResponse(
    val id: Long,
    @param:JsonProperty("kakao_account") val kakaoAccount: KakaoAccount,
) {
    data class KakaoAccount(
        val email: String,
    )
}
