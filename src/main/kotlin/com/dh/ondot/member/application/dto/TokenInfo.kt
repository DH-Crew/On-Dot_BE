package com.dh.ondot.member.application.dto

import java.time.Instant

data class TokenInfo(
    val tokenId: String,
    val memberId: String,
    val expiration: Instant,
)
