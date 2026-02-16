package com.dh.ondot.schedule.infra.outbox

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxMessageHandler(
    private val router: OutboxMessageRouter,
    private val repository: OutboxMessageRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleInNewTx(msg: OutboxMessage) {
        try {
            router.route(msg)
            msg.markSendSuccess()
        } catch (ex: Exception) {
            msg.markSendFail()
            log.error("Outbox 처리 실패 id={}", msg.id, ex)
        } finally {
            // detached 상태의 msg managed 상태로 변경하기 위해 merge
            repository.save(msg)
        }
    }
}
