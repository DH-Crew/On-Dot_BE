package com.dh.ondot.notification.domain

import com.dh.ondot.core.util.TimeUtils
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "subway_alerts")
class SubwayAlert(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subway_alert_id")
    val id: Long = 0L,

    @Column(name = "title")
    val title: String = "",

    @Column(name = "content", columnDefinition = "TEXT")
    val content: String = "",

    @Column(name = "line_name")
    val lineName: String = "",

    @Column(name = "start_time")
    val startTime: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, unique = true)
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        fun create(
            title: String,
            content: String,
            lineName: String,
            startTime: LocalDateTime,
            createdAt: LocalDateTime,
        ): SubwayAlert =
            SubwayAlert(
                title = title,
                content = content,
                lineName = lineName,
                startTime = TimeUtils.toInstant(startTime),
                createdAt = TimeUtils.toInstant(createdAt),
            )
    }
}
