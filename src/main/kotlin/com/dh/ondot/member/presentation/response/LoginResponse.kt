package com.dh.ondot.member.presentation.response

import com.dh.ondot.member.application.dto.LoginResult

data class LoginResponse(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
) {
    companion object {
        fun from(result: LoginResult): LoginResponse =
            LoginResponse(result.memberId, result.accessToken, result.refreshToken, result.isNewMember)
    }
}
