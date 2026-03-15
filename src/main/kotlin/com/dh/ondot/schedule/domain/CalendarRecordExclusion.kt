package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "calendar_record_exclusions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_exclusion_member_schedule_date",
            columnNames = ["member_id", "schedule_id", "excluded_date"],
        )
    ],
)
class CalendarRecordExclusion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exclusion_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "schedule_id", nullable = false)
    val scheduleId: Long,

    @Column(name = "excluded_date", nullable = false)
    val excludedDate: LocalDate,
) : BaseTimeEntity() {
    companion object {
        fun create(memberId: Long, scheduleId: Long, excludedDate: LocalDate): CalendarRecordExclusion =
            CalendarRecordExclusion(memberId = memberId, scheduleId = scheduleId, excludedDate = excludedDate)
    }
}
