package com.dh.ondot.schedule.infra.api

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.schedule.presentation.response.ScheduleParsedResponse
import com.dh.ondot.schedule.core.exception.OpenAiParsingException
import com.dh.ondot.schedule.core.exception.UnavailableOpenAiServerException
import com.dh.ondot.schedule.core.exception.UnhandledOpenAiException
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.converter.BeanOutputConverter
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@Component
class OpenAiPromptApi(
    private val chat: ChatClient,
) {
    companion object {
        private val CONVERTER = BeanOutputConverter(ScheduleParsedResponse::class.java)
        private val SYSTEM_TMPL = """
            Date: %s KST.
            Parse the Korean appointment sentence into JSON:
            {"departurePlaceTitle":"...","appointmentAt":"yyyy-MM-dd'T'HH:mm:ss"}
            Rules:
            - Use relative dates (e.g., "tomorrow") from Date.
            - Fix OCR typos to real Korean place names.
            - Bare hours (e.g., "6시") → assume 18:00.
            - No 24‑hr notation; don't roll times into the next day.
            """.trimIndent()
    }

    @Retryable(
        retryFor = [HttpServerErrorException::class],
        maxAttempts = 2,
        backoff = Backoff(delay = 500)
    )
    fun parseNaturalLanguage(userText: String): ScheduleParsedResponse {
        val systemPrompt = SYSTEM_TMPL.format(
            TimeUtils.nowSeoulDate()
        )

        try {
            return chat.prompt()
                .system(systemPrompt)
                .user(userText)
                .call()
                .entity(CONVERTER)
        } catch (ex: HttpClientErrorException) {
            throw OpenAiParsingException()
        }
    }

    @Recover
    fun recover(ex: HttpServerErrorException, userText: String): ScheduleParsedResponse {
        // 5xx 재시도 모두 실패했을 때
        throw UnavailableOpenAiServerException()
    }

    @Recover
    fun recoverUnhandled(t: Throwable, userText: String): ScheduleParsedResponse {
        throw UnhandledOpenAiException()
    }
}
