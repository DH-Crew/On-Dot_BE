package com.dh.ondot.schedule.infra.event;

import com.dh.ondot.schedule.app.port.EventSerializer;
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.dh.ondot.schedule.infra.outbox.OutboxMessage;
import com.dh.ondot.schedule.infra.outbox.OutboxMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ScheduleInternalEventRecordListener {
    private final EventSerializer serializer;
    private final OutboxMessageRepository outboxMessageRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void recordEvent(QuickScheduleRequestedEvent event) {
        String json = serializer.serialize(event);
        outboxMessageRepository.save(OutboxMessage.init(
                "QUICK_SCHEDULE_REQUESTED",
                json
        ));
    }
}
