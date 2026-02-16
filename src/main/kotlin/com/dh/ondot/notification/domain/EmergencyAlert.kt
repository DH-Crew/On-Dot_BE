package com.dh.ondot.notification.domain

import com.dh.ondot.core.util.TimeUtils
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime

@Entity
@Table(name = "emergency_alerts")
class EmergencyAlert(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_alert_id")
    val id: Long = 0L,

    @Column(name = "content", columnDefinition = "TEXT")
    val content: String = "",

    @Column(name = "region_name")
    val regionName: String = "",

    @Column(name = "created_at", nullable = false, unique = true)
    val createdAt: Instant = Instant.now(),
) {
    companion object {
        fun create(content: String, regionName: String, createdAt: LocalDateTime): EmergencyAlert =
            EmergencyAlert(
                content = content,
                regionName = regionName,
                createdAt = TimeUtils.toInstant(createdAt),
            )
    }
}
