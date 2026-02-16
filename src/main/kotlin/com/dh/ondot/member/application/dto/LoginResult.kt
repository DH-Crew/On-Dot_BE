package com.dh.ondot.member.application.dto

data class LoginResult(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
)
