package com.dh.ondot.member.api.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class UpdatePreparationTimeRequest(
    @field:NotNull
    @field:Min(value = 1, message = "준비 시간은 최소 1분이어야 합니다.")
    @field:Max(value = 600, message = "준비 시간은 최대 600분까지 가능합니다.")
    val preparationTime: Int,
)
