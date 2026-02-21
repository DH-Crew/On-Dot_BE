package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.schedule.core.exception.MaxAiUsageLimitExceededException
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "ai_usages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["member_id", "usage_date"])
    ]
)
class AiUsage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "usage_date", nullable = false)
    val usageDate: LocalDate,

    @Column(name = "count", nullable = false)
    var count: Int,
) : BaseTimeEntity() {

    fun increase() {
        if (count >= 10) {
            throw MaxAiUsageLimitExceededException(memberId, LocalDate.now())
        }
        count++
    }

    companion object {
        fun newForToday(memberId: Long, date: LocalDate): AiUsage =
            AiUsage(memberId = memberId, usageDate = date, count = 1)
    }
}
