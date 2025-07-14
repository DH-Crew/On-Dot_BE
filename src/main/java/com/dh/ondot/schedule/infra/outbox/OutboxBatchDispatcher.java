package com.dh.ondot.schedule.infra.outbox;

import com.dh.ondot.core.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxBatchDispatcher {
    private final OutboxMessageHandler handler;
    private final OutboxMessageRepository repository;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void dispatchPendingBatch() {
        List<OutboxMessage> newMessages = repository.findTop100ByStatusOrderById(MessageStatus.INIT);
        dispatchBatch(newMessages);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void dispatchRetryBatch() {
        PageRequest pageable = PageRequest.of(0, 100);
        List<OutboxMessage> retryableMessages = repository.findRetryableMessages(DateTimeUtils.nowSeoulInstant(), pageable);
        dispatchBatch(retryableMessages);
    }

    private void dispatchBatch(List<OutboxMessage> messages) {
        for (OutboxMessage msg : messages) {
            try {
                handler.handleInNewTx(msg);
            } catch (Exception e) {
                log.warn("Failed to process outbox message. It will be retried if possible. messageId={}", msg.getId(), e);
            }
        }
    }
}
