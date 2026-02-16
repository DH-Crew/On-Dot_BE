package com.dh.ondot.member.application.command

data class DeleteMemberCommand(
    val withdrawalReasonId: Long,
    val customReason: String?,
)
