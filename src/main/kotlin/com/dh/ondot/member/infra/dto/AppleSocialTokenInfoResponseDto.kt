package com.dh.ondot.member.infra.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class AppleSocialTokenInfoResponseDto(
    @param:JsonProperty("access_token") val accessToken: String,
    @param:JsonProperty("expires_in") val expiresIn: Long,
    @param:JsonProperty("id_token") val idToken: String,
    @param:JsonProperty("refresh_token") val refreshToken: String,
    @param:JsonProperty("token_type") val tokenType: String,
)
