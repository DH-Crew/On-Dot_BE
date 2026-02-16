package com.dh.ondot.schedule.infra.outbox

import com.dh.ondot.core.util.TimeUtils
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxBatchDispatcher(
    private val handler: OutboxMessageHandler,
    private val repository: OutboxMessageRepository,
) {
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun dispatchPendingBatch() {
        val newMessages = repository.findTop100ByStatusOrderById(MessageStatus.INIT)
        dispatchBatch(newMessages)
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun dispatchRetryBatch() {
        val pageable = PageRequest.of(0, 100)
        val retryableMessages = repository.findRetryableMessages(TimeUtils.nowSeoulInstant(), pageable)
        dispatchBatch(retryableMessages)
    }

    private fun dispatchBatch(messages: List<OutboxMessage>) {
        for (msg in messages) {
            handler.handleInNewTx(msg)
        }
    }
}
