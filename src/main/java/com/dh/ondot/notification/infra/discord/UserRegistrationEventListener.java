package com.dh.ondot.notification.infra.discord;

import com.dh.ondot.member.domain.event.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.dh.ondot.core.config.AsyncConstants.DISCORD_ASYNC_TASK_EXECUTOR;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationEventListener {
    private final DiscordWebhookClient discordWebhookClient;
    private final DiscordMessageTemplate discordMessageTemplate;

    @Async(DISCORD_ASYNC_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistration(UserRegistrationEvent event) {
        try {
            String message = discordMessageTemplate.createUserRegistrationMessage(
                event.email(),
                event.oauthProvider(),
                event.totalMemberCount()
            );
            
            discordWebhookClient.sendMessage(message);
        } catch (Exception e) {
            log.error("[DISCORD FAIL] 회원 가입 완료 디스코드 메시지를 전송하는데 실패했습니다.[ memberId={} ]", event.memberId(), e);
        }
    }
}
