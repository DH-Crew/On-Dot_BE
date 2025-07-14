package com.dh.ondot.schedule.infra.event;

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
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper mapper;

    @SneakyThrows(JsonProcessingException.class)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void record(QuickScheduleRequestedEvent event) {
        String json = mapper.writeValueAsString(event);
        outboxMessageRepository.save(OutboxMessage.init(
                "QUICK_SCHEDULE_REQUESTED",
                json
        ));
    }
}
