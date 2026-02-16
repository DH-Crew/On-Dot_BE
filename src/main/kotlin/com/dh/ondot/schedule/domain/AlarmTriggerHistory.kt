package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.domain.enums.AlarmTriggerAction
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "alarm_trigger_histories")
class AlarmTriggerHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "alarm_id", nullable = false)
    val alarmId: Long,

    @Column(name = "schedule_id", nullable = false)
    val scheduleId: Long,

    @Column(name = "triggered_at", nullable = false)
    val triggeredAt: Instant,

    @Column(name = "responded_at")
    val respondedAt: Instant? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    val action: AlarmTriggerAction,

    @Column(name = "device_type", length = 20)
    val deviceType: String? = null,
) : BaseTimeEntity() {

    protected constructor() : this(
        memberId = 0L,
        alarmId = 0L,
        scheduleId = 0L,
        triggeredAt = Instant.now(),
        action = AlarmTriggerAction.SCHEDULED,
    )

    companion object {
        @JvmStatic
        fun record(
            memberId: Long,
            alarmId: Long,
            scheduleId: Long,
            respondedAt: Instant,
            action: String,
            deviceType: String,
        ): AlarmTriggerHistory = AlarmTriggerHistory(
            memberId = memberId,
            alarmId = alarmId,
            scheduleId = scheduleId,
            triggeredAt = TimeUtils.nowSeoulInstant(),
            respondedAt = respondedAt,
            action = AlarmTriggerAction.from(action),
            deviceType = deviceType,
        )
    }
}
