package com.dh.ondot.schedule.infra.outbox;

import com.dh.ondot.schedule.app.handler.QuickScheduleInternalEventHandler;
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMessageRouter {
    private static final String QUICK_SCHEDULE_EVT = "QUICK_SCHEDULE_REQUESTED";

    private final QuickScheduleInternalEventHandler eventHandler;
    private final ObjectMapper mapper;

    public void route(OutboxMessage msg) throws Exception {
        switch (msg.getEventType()) {
            case QUICK_SCHEDULE_EVT -> {
                QuickScheduleRequestedEvent event = mapper.readValue(msg.getPayload(), QuickScheduleRequestedEvent.class);
                eventHandler.handleEvent(event);
            }
            default -> throw new IllegalStateException("Unknown eventType " + msg.getEventType());
        }
    }
}
