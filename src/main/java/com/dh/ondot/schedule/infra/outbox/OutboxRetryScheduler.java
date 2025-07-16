package com.dh.ondot.schedule.infra.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler {
    private final OutboxBatchDispatcher dispatcher;

    @Scheduled(fixedDelay = 60_000)
    public void processPendingOutboxMessages() {
        dispatcher.dispatchRetryBatch();
        dispatcher.dispatchPendingBatch();
    }
}
