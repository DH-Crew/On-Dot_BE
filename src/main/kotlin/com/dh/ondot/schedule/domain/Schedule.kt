package com.dh.ondot.schedule.domain

import com.dh.ondot.core.AggregateRoot
import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.converter.RepeatDaysConverter
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.enums.TransportType
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.SortedSet

@AggregateRoot
@Entity
@Table(name = "schedules")
class Schedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    var memberId: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "departure_place_id", nullable = false)
    var departurePlace: Place? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "arrival_place_id", nullable = false)
    var arrivalPlace: Place? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "preparation_alarm_id", nullable = false)
    var preparationAlarm: Alarm? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "departure_alarm_id", nullable = false)
    var departureAlarm: Alarm? = null,

    @Column(name = "title", nullable = false)
    var title: String = "",

    @Column(name = "is_repeat", nullable = false, columnDefinition = "TINYINT(1)")
    var isRepeat: Boolean = false,

    @Convert(converter = RepeatDaysConverter::class)
    @Column(name = "repeat_days")
    var repeatDays: SortedSet<Int>? = null,

    @Column(name = "appointment_at", nullable = false)
    var appointmentAt: Instant = Instant.now(),

    @Column(name = "is_medication_required", nullable = false, columnDefinition = "TINYINT(1)")
    var isMedicationRequired: Boolean = false,

    @Column(name = "preparation_note", length = 100)
    var preparationNote: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    var transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
) : BaseTimeEntity() {

    fun registerPlaces(departurePlace: Place, arrivalPlace: Place) {
        this.departurePlace = departurePlace
        this.arrivalPlace = arrivalPlace
    }

    fun updateCore(
        title: String, isRepeat: Boolean,
        repeatDays: SortedSet<Int>?, appointmentAt: LocalDateTime,
    ) {
        this.title = title
        this.isRepeat = isRepeat
        this.repeatDays = if (isRepeat) repeatDays else null
        this.appointmentAt = TimeUtils.toInstant(appointmentAt)
    }

    fun isAppointmentTimeChanged(newAppointmentAt: LocalDateTime): Boolean =
        this.appointmentAt != TimeUtils.toInstant(newAppointmentAt)

    fun setupQuickSchedule(memberId: Long, appointmentAt: LocalDateTime) {
        this.memberId = memberId
        this.title = "새로운 일정"
        this.isRepeat = false
        this.appointmentAt = TimeUtils.toInstant(appointmentAt)
    }

    fun switchAlarm(enabled: Boolean) {
        preparationAlarm!!.changeEnabled(enabled)
        departureAlarm!!.changeEnabled(enabled)
    }

    fun hasAnyActiveAlarm(): Boolean =
        preparationAlarm!!.isEnabled || departureAlarm!!.isEnabled

    /**
     * 반복 설정에 따라 다음 알람 시간을 계산한다
     * hasAnyActiveAlarm이 true인 경우 사용한다
     * 일회성 알람: preparationAlarm과 departureAlarm 중 활성화된 것 중 현재 시간 이후 가장 빠른 것 반환
     * 반복 알람: repeatDays를 고려해서 현재 시간 이후 가장 먼저 울릴 알람 시간 계산
     */
    fun calculateNextAlarmAt(): Instant? {
        if (!isRepeat) {
            val prepAlarmAt = if (preparationAlarm!!.isEnabled) preparationAlarm!!.triggeredAt else null
            val deptAlarmAt = if (departureAlarm!!.isEnabled) departureAlarm!!.triggeredAt else null
            return TimeUtils.findEarliestAfterNow(prepAlarmAt, deptAlarmAt)
        }

        // 반복 일정 처리
        val nextPrepAlarmAt = if (preparationAlarm!!.isEnabled)
            calculateNextRepeatTime(preparationAlarm!!.triggeredAt) else null

        val nextDeptAlarmAt = if (departureAlarm!!.isEnabled)
            calculateNextRepeatTime(departureAlarm!!.triggeredAt) else null

        return TimeUtils.findEarliestAfterNow(nextPrepAlarmAt, nextDeptAlarmAt)
    }

    fun getNextRepeatAlarmTime(baseAlarmTime: Instant): Instant? {
        if (!isRepeat) {
            return baseAlarmTime
        }
        return calculateNextRepeatTime(baseAlarmTime)
    }

    /**
     * 반복 설정에 따라 다음 알람 시간을 계산한다.
     * 현재 시간 이후 7일 이내에서 해당 요일에 맞는 가장 빠른 시간을 찾는다.
     */
    private fun calculateNextRepeatTime(baseAlarmTime: Instant): Instant? {
        val now = Instant.now()
        val alarmTime: LocalTime = TimeUtils.toSeoulTime(baseAlarmTime)!!
        val today: LocalDate = TimeUtils.nowSeoulDate()

        for (daysAhead in 0..7) {
            val candidateDate = today.plusDays(daysAhead.toLong())

            if (isScheduledForDayOfWeek(candidateDate)) {
                val candidateTime = TimeUtils.toInstant(candidateDate.atTime(alarmTime))

                if (candidateTime.isAfter(now)) {
                    return candidateTime
                }
            }
        }

        return null
    }

    // 특정 날짜가 반복 요일에 해당하는지 확인한다
    private fun isScheduledForDayOfWeek(date: LocalDate): Boolean {
        // repeatDays: [1(일) .. 7(토)], DayOfWeek: [1(월) .. 7(일)]
        // 변환 로직: 월(1)->2, 화(2)->3, ..., 토(6)->7, 일(7)->1
        val dayValue = (date.dayOfWeek.value % 7) + 1
        return repeatDays!!.contains(dayValue)
    }

    companion object {
        @JvmStatic
        fun createSchedule(
            memberId: Long, departurePlace: Place, arrivalPlace: Place,
            preparationAlarm: Alarm, departureAlarm: Alarm, title: String,
            isRepeat: Boolean, repeatDays: SortedSet<Int>?, appointmentAt: LocalDateTime,
            isMedicationRequired: Boolean, preparationNote: String?,
            transportType: TransportType = TransportType.PUBLIC_TRANSPORT,
        ): Schedule = Schedule(
            memberId = memberId,
            departurePlace = departurePlace,
            arrivalPlace = arrivalPlace,
            preparationAlarm = preparationAlarm,
            departureAlarm = departureAlarm,
            title = title,
            isRepeat = isRepeat,
            repeatDays = if (isRepeat) repeatDays else null,
            appointmentAt = TimeUtils.toInstant(appointmentAt),
            isMedicationRequired = isMedicationRequired,
            preparationNote = preparationNote,
            transportType = transportType,
        )

        @JvmStatic
        fun createWithDefaultAlarmSetting(
            alarmMode: AlarmMode, snooze: Snooze, sound: Sound,
            appointmentAt: LocalDateTime, estimatedTime: Int, preparationTime: Int,
        ): Schedule = Schedule(
            preparationAlarm = Alarm.createPreparationAlarmWithDefaultSetting(
                alarmMode, snooze, sound,
                appointmentAt, estimatedTime, preparationTime,
            ),
            departureAlarm = Alarm.createDepartureAlarmWithDefaultSetting(
                alarmMode, snooze, sound,
                appointmentAt, estimatedTime,
            ),
            appointmentAt = TimeUtils.toInstant(appointmentAt),
        )
    }
}
