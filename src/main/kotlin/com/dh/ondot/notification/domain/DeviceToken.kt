package com.dh.ondot.notification.domain

import com.dh.ondot.core.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_device_token_fcm", columnNames = ["fcm_token"])
    ]
)
class DeviceToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    val id: Long = 0L,

    @Column(name = "member_id", nullable = false)
    var memberId: Long,

    @Column(name = "fcm_token", nullable = false, length = 512)
    var fcmToken: String,

    @Column(name = "device_type", nullable = false, length = 20)
    var deviceType: String,
) : BaseTimeEntity() {

    fun updateOwner(memberId: Long, deviceType: String) {
        this.memberId = memberId
        this.deviceType = deviceType
    }

    companion object {
        fun create(memberId: Long, fcmToken: String, deviceType: String): DeviceToken =
            DeviceToken(memberId = memberId, fcmToken = fcmToken, deviceType = deviceType)
    }
}
