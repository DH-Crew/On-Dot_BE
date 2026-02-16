package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "odsay_usages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["usage_date"])
    ]
)
class OdsayUsage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    val id: Long = 0L,

    @Column(name = "usage_date", nullable = false)
    val usageDate: LocalDate,

    @Column(name = "count", nullable = false)
    var count: Int,
) : BaseTimeEntity() {

    fun getRemainingUsage(): Int = maxOf(0, DAILY_LIMIT - count)

    companion object {
        private const val DAILY_LIMIT = 1000

        @JvmStatic
        fun newForToday(date: LocalDate): OdsayUsage =
            OdsayUsage(usageDate = date, count = 1)
    }
}
