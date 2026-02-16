package com.dh.ondot.schedule.infra.outbox

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxRetryScheduler(
    private val dispatcher: OutboxBatchDispatcher,
) {
//    @Scheduled(fixedDelay = 60_000)
    fun processPendingOutboxMessages() {
        dispatcher.dispatchRetryBatch()
        dispatcher.dispatchPendingBatch()
    }
}
