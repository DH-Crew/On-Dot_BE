package com.dh.ondot.schedule.fixture

import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import java.time.LocalDateTime
import java.util.SortedSet
import java.util.TreeSet

object ScheduleFixture {

    fun builder(): ScheduleBuilder = ScheduleBuilder()

    fun defaultSchedule(): Schedule = builder().build()

    fun repeatSchedule(repeatDays: SortedSet<Int>): Schedule =
        builder()
            .isRepeat(true)
            .repeatDays(repeatDays)
            .build()

    class ScheduleBuilder {
        private var memberId: Long = 1L
        private var title: String = "테스트 일정"
        private var isRepeat: Boolean = false
        private var repeatDays: SortedSet<Int>? = null
        private var appointmentAt: LocalDateTime = LocalDateTime.now().plusDays(7).withHour(16).withMinute(0).withSecond(0).withNano(0)
        private var isMedicationRequired: Boolean = false
        private var preparationNote: String = "준비 메모"
        private var departurePlace: Place = PlaceFixture.defaultDeparturePlace()
        private var arrivalPlace: Place = PlaceFixture.defaultArrivalPlace()
        private var preparationAlarm: Alarm = AlarmFixture.defaultPreparationAlarm()
        private var departureAlarm: Alarm = AlarmFixture.defaultDepartureAlarm()

        fun memberId(memberId: Long): ScheduleBuilder = apply { this.memberId = memberId }
        fun isRepeat(isRepeat: Boolean): ScheduleBuilder = apply { this.isRepeat = isRepeat }
        fun repeatDays(repeatDays: SortedSet<Int>?): ScheduleBuilder = apply { this.repeatDays = repeatDays }
        fun appointmentAt(appointmentAt: LocalDateTime): ScheduleBuilder = apply { this.appointmentAt = appointmentAt }

        fun disabledAlarms(): ScheduleBuilder = apply {
            this.preparationAlarm = AlarmFixture.disabledAlarm()
            this.departureAlarm = AlarmFixture.disabledAlarm()
        }

        fun onlyPreparationAlarmEnabled(): ScheduleBuilder = apply {
            this.preparationAlarm = AlarmFixture.enabledAlarm(this.appointmentAt.minusHours(1))
            this.departureAlarm = AlarmFixture.disabledAlarm()
        }

        fun onlyDepartureAlarmEnabled(): ScheduleBuilder = apply {
            this.preparationAlarm = AlarmFixture.disabledAlarm()
            this.departureAlarm = AlarmFixture.enabledAlarm(this.appointmentAt.minusMinutes(30))
        }

        fun build(): Schedule = Schedule.createSchedule(
            memberId, departurePlace, arrivalPlace,
            preparationAlarm, departureAlarm, title,
            isRepeat, repeatDays, appointmentAt,
            isMedicationRequired, preparationNote
        )
    }

    fun weekdays(): SortedSet<Int> {
        val days = TreeSet<Int>()
        days.add(2) // 월
        days.add(3) // 화
        days.add(4) // 수
        days.add(5) // 목
        days.add(6) // 금
        return days
    }

    fun weekends(): SortedSet<Int> {
        val days = TreeSet<Int>()
        days.add(1) // 일
        days.add(7) // 토
        return days
    }
}
