package com.dh.ondot.schedule.infra.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxMessageHandler {
    private final OutboxMessageRouter router;
    private final OutboxMessageRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleInNewTx(OutboxMessage msg) {
        try {
            router.route(msg);
            msg.markSendSuccess();
        } catch (Exception ex) {
            msg.markSendFail();
            log.error("Outbox 처리 실패 id={}", msg.getId(), ex);
        } finally {
            // detached 상태의 msg managed 상태로 변경하기 위해 merge
            repository.save(msg);
        }
    }
}
