package com.dh.ondot.member.presentation.response

data class LoginResponse(
    val memberId: Long,
    val accessToken: String,
    val refreshToken: String,
    val isNewMember: Boolean,
) {
    companion object {
        fun of(
            memberId: Long,
            accessToken: String,
            refreshToken: String,
            isNewMember: Boolean,
        ): LoginResponse =
            LoginResponse(memberId, accessToken, refreshToken, isNewMember)
    }
}
