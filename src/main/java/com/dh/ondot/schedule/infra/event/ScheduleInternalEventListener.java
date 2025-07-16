package com.dh.ondot.schedule.infra.event;

import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.dh.ondot.schedule.infra.outbox.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.dh.ondot.core.config.AsyncConstants.EVENT_ASYNC_TASK_EXECUTOR;

@Component
@RequiredArgsConstructor
public class ScheduleInternalEventListener {
    private final OutboxBatchDispatcher dispatcher;

    @Async(EVENT_ASYNC_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processOutboxOnCommit(QuickScheduleRequestedEvent ignored) {
        dispatcher.dispatchPendingBatch();
    }
}
