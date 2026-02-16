package com.dh.ondot.member.api.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.Member
import java.time.LocalDateTime

data class PreparationTimeResponse(
    val preparationTime: Int,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member): PreparationTimeResponse =
            PreparationTimeResponse(
                preparationTime = member.preparationTime!!,
                updatedAt = TimeUtils.toSeoulDateTime(member.updatedAt)!!,
            )
    }
}
