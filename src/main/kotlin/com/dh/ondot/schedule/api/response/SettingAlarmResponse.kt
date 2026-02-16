package com.dh.ondot.schedule.api.response

import com.dh.ondot.schedule.domain.Alarm

data class SettingAlarmResponse(
    val preparationAlarm: AlarmDto,
    val departureAlarm: AlarmDto,
) {
    companion object {
        @JvmStatic
        fun from(preparation: Alarm, departure: Alarm): SettingAlarmResponse {
            return SettingAlarmResponse(
                preparationAlarm = AlarmDto.of(preparation),
                departureAlarm = AlarmDto.of(departure),
            )
        }
    }
}
