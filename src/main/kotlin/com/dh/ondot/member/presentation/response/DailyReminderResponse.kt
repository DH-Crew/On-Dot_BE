package com.dh.ondot.member.presentation.response

data class DailyReminderResponse(
    val enabled: Boolean,
) {
    companion object {
        fun from(enabled: Boolean): DailyReminderResponse =
            DailyReminderResponse(enabled = enabled)
    }
}
