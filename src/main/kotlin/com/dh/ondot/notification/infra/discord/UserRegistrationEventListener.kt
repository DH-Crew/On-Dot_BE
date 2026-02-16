package com.dh.ondot.notification.infra.discord

import com.dh.ondot.core.config.AsyncConstants.DISCORD_ASYNC_TASK_EXECUTOR
import com.dh.ondot.member.domain.event.UserRegistrationEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class UserRegistrationEventListener(
    private val discordWebhookClient: DiscordWebhookClient,
    private val discordMessageTemplate: DiscordMessageTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async(DISCORD_ASYNC_TASK_EXECUTOR)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleUserRegistration(event: UserRegistrationEvent) {
        try {
            val message = discordMessageTemplate.createUserRegistrationMessage(
                event.email,
                event.oauthProvider,
                event.totalMemberCount,
                event.mobileType,
            )

            discordWebhookClient.sendMessage(message)
        } catch (e: Exception) {
            log.error("[DISCORD FAIL] 회원 가입 완료 디스코드 메시지를 전송하는데 실패했습니다.[ memberId={} ]", event.memberId, e)
        }
    }
}
