package com.dh.ondot.schedule.fixture

import com.dh.ondot.schedule.domain.Alarm
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.Schedule
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
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
        private var id: Long? = null
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
        private var deletedAt: Instant? = null

        private var createdAt: Instant? = null

        fun id(id: Long): ScheduleBuilder = apply { this.id = id }
        fun memberId(memberId: Long): ScheduleBuilder = apply { this.memberId = memberId }
        fun title(title: String): ScheduleBuilder = apply { this.title = title }
        fun deletedAt(deletedAt: Instant?): ScheduleBuilder = apply { this.deletedAt = deletedAt }
        fun deleted(): ScheduleBuilder = apply { this.deletedAt = Instant.now() }
        fun isRepeat(isRepeat: Boolean): ScheduleBuilder = apply { this.isRepeat = isRepeat }
        fun repeatDays(repeatDays: SortedSet<Int>?): ScheduleBuilder = apply { this.repeatDays = repeatDays }
        fun appointmentAt(appointmentAt: LocalDateTime): ScheduleBuilder = apply { this.appointmentAt = appointmentAt }
        fun createdAt(createdAt: Instant): ScheduleBuilder = apply { this.createdAt = createdAt }

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

        fun build(): Schedule {
            val schedule = Schedule.createSchedule(
                memberId, departurePlace, arrivalPlace,
                preparationAlarm, departureAlarm, title,
                isRepeat, repeatDays, appointmentAt,
                isMedicationRequired, preparationNote
            )
            id?.let {
                val idField = Schedule::class.java.getDeclaredField("id")
                idField.isAccessible = true
                idField.set(schedule, it)
            }
            deletedAt?.let { schedule.deletedAt = it }
            createdAt?.let { schedule.createdAt = it }
            return schedule
        }
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

    fun allDays(): SortedSet<Int> = TreeSet((1..7).toList())

    private val SEOUL_ZONE = ZoneId.of("Asia/Seoul")

    fun instantOf(date: LocalDate, time: LocalTime): Instant =
        date.atTime(time).atZone(SEOUL_ZONE).toInstant()
}
