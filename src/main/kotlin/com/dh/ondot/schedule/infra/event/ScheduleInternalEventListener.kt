package com.dh.ondot.schedule.infra.event

import com.dh.ondot.core.config.AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent
import com.dh.ondot.schedule.infra.outbox.OutboxBatchDispatcher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ScheduleInternalEventListener(
    private val dispatcher: OutboxBatchDispatcher,
) {
    @Async(EVENT_ASYNC_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun processOutboxOnCommit(ignored: QuickScheduleRequestedEvent) {
        dispatcher.dispatchPendingBatch()
    }
}
