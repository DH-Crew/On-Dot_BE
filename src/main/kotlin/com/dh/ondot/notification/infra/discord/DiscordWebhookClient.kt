package com.dh.ondot.notification.infra.discord

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DiscordWebhookClient(
    private val restClient: RestClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${external-api.discord.webhook.url}")
    private lateinit var discordUrl: String

    fun sendMessage(message: String) {
        val payload = createPayload(message)

        restClient.post()
            .uri(discordUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .toBodilessEntity()
    }

    private fun createPayload(message: String): Map<String, Any> {
        return mapOf("content" to message)
    }
}
