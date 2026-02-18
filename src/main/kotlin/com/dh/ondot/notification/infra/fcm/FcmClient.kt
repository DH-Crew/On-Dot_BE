package com.dh.ondot.notification.infra.fcm

import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FcmClient {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_TOKENS_PER_REQUEST = 500
    }

    fun sendToTokens(tokens: List<String>, title: String, body: String): List<String> {
        if (tokens.isEmpty()) return emptyList()

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("FirebaseApp is not initialized. Skipping push notification.")
            return emptyList()
        }

        val invalidTokens = mutableListOf<String>()
        tokens.chunked(MAX_TOKENS_PER_REQUEST).forEach { chunk ->
            try {
                val message = MulticastMessage.builder()
                    .setNotification(
                        Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build()
                    )
                    .addAllTokens(chunk)
                    .build()

                val response = FirebaseMessaging.getInstance().sendEachForMulticast(message)

                response.responses.forEachIndexed { index, sendResponse ->
                    if (!sendResponse.isSuccessful) {
                        val error = sendResponse.exception
                        log.warn("FCM send failed for token[{}]: {}", index, error?.messagingErrorCode)
                        if (error?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
                            invalidTokens.add(chunk[index])
                        }
                    }
                }

                log.info(
                    "FCM multicast result: success={}, failure={}",
                    response.successCount,
                    response.failureCount
                )
            } catch (e: Exception) {
                log.error("FCM multicast send failed for chunk of {} tokens", chunk.size, e)
            }
        }
        return invalidTokens
    }
}
