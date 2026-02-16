package com.dh.ondot.schedule.infra.event

import com.dh.ondot.schedule.core.EventSerializer
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent
import com.dh.ondot.schedule.infra.outbox.OutboxMessage
import com.dh.ondot.schedule.infra.outbox.OutboxMessageRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ScheduleInternalEventRecordListener(
    private val serializer: EventSerializer,
    private val outboxMessageRepository: OutboxMessageRepository,
) {
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun recordEvent(event: QuickScheduleRequestedEvent) {
        val json = serializer.serialize(event)
        outboxMessageRepository.save(
            OutboxMessage.init(
                "QUICK_SCHEDULE_REQUESTED",
                json
            )
        )
    }
}
