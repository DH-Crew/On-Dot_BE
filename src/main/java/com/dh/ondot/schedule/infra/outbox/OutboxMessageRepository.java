package com.dh.ondot.schedule.infra.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findTop100ByStatusOrderById(MessageStatus status);

    @Query("SELECT m FROM OutboxMessage m WHERE m.status = 'SEND_FAIL' AND m.nextTryAt <= :now ORDER BY m.id ASC")
    List<OutboxMessage> findRetryableMessages(@Param("now") Instant now, Pageable pageable);
}
