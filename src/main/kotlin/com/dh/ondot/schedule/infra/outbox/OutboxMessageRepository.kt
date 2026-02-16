package com.dh.ondot.schedule.infra.outbox

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface OutboxMessageRepository : JpaRepository<OutboxMessage, Long> {
    fun findTop100ByStatusOrderById(status: MessageStatus): List<OutboxMessage>

    @Query("SELECT m FROM OutboxMessage m WHERE m.status = 'SEND_FAIL' AND m.nextTryAt <= :now ORDER BY m.id ASC")
    fun findRetryableMessages(@Param("now") now: Instant, pageable: Pageable): List<OutboxMessage>
}
