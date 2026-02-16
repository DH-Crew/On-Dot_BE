package com.dh.ondot.schedule.infra.outbox

import com.dh.ondot.schedule.application.handler.QuickScheduleInternalEventHandler
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class OutboxMessageRouter(
    private val eventHandler: QuickScheduleInternalEventHandler,
    private val mapper: ObjectMapper,
) {
    fun route(msg: OutboxMessage) {
        when (msg.eventType) {
            QUICK_SCHEDULE_EVT -> {
                val event = mapper.readValue(msg.payload, QuickScheduleRequestedEvent::class.java)
                eventHandler.handleEvent(event)
            }
            else -> throw IllegalStateException("Unknown eventType " + msg.eventType)
        }
    }

    companion object {
        private const val QUICK_SCHEDULE_EVT = "QUICK_SCHEDULE_REQUESTED"
    }
}
