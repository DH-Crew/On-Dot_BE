package com.dh.ondot.schedule.domain

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.schedule.domain.enums.ApiType
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "api_usages",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["api_type", "usage_date"])
    ]
)
class ApiUsage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    val id: Long = 0L,

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false)
    val apiType: ApiType,

    @Column(name = "usage_date", nullable = false)
    val usageDate: LocalDate,

    @Column(name = "count", nullable = false)
    var count: Int,
) : BaseTimeEntity() {

    fun getRemainingUsage(): Int = maxOf(0, DAILY_LIMIT - count)

    companion object {
        const val DAILY_LIMIT = 1000

        fun newForToday(apiType: ApiType, date: LocalDate): ApiUsage =
            ApiUsage(apiType = apiType, usageDate = date, count = 1)
    }
}
