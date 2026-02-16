package com.dh.ondot.schedule.infra.outbox

import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.core.util.TimeUtils
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "outbox_message",
    indexes = [Index(name = "idx_status", columnList = "status")]
)
class OutboxMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val eventType: String = "",

    @Lob
    @Column(columnDefinition = "json")
    val payload: String = "",

    @Enumerated(EnumType.STRING)
    var status: MessageStatus = MessageStatus.INIT,

    @Column(nullable = false)
    var tryCount: Int = 0,

    var nextTryAt: Instant? = null,
) : BaseTimeEntity() {

    companion object {
        private const val MAX_TRY_COUNT = 5

        fun init(eventType: String, payload: String): OutboxMessage {
            return OutboxMessage(
                eventType = eventType,
                payload = payload,
                status = MessageStatus.INIT,
            )
        }
    }

    fun markSendSuccess() {
        status = MessageStatus.SEND_SUCCESS
    }

    fun markSendFail() {
        tryCount++

        if (tryCount >= MAX_TRY_COUNT) {
            status = MessageStatus.DEAD
        } else {
            status = MessageStatus.SEND_FAIL
            nextTryAt = TimeUtils.toInstant(TimeUtils.nowSeoulDateTime().plusMinutes(tryCount.toLong() * 5))
        }
    }
}
