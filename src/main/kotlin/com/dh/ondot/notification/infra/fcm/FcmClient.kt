package com.dh.ondot.notification.infra.fcm

import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmClient {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendToTokens(tokens: List<String>, title: String, body: String): List<String> {
        if (tokens.isEmpty()) return emptyList()

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build()
            )
            .addAllTokens(tokens)
            .build()

        val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)

        val invalidTokens = mutableListOf<String>()
        response.responses.forEachIndexed { index, sendResponse ->
            if (!sendResponse.isSuccessful) {
                val error = sendResponse.exception
                log.warn("FCM send failed for token[{}]: {}", index, error?.messagingErrorCode)
                if (error?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                    invalidTokens.add(tokens[index])
                }
            }
        }

        log.info(
            "FCM multicast result: success={}, failure={}",
            response.successCount,
            response.failureCount
        )
        return invalidTokens
    }
}
