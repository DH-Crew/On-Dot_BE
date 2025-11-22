package com.dh.ondot.schedule.infra.api;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.schedule.api.response.ScheduleParsedResponse;
import com.dh.ondot.schedule.core.exception.OpenAiParsingException;
import com.dh.ondot.schedule.core.exception.UnavailableOpenAiServerException;
import com.dh.ondot.schedule.core.exception.UnhandledOpenAiException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
@RequiredArgsConstructor
public class OpenAiPromptApi {
    private static final BeanOutputConverter<ScheduleParsedResponse> CONVERTER = new BeanOutputConverter<>(ScheduleParsedResponse.class);
    private static final String SYSTEM_TMPL = """
        Date: %s KST.
        Parse the Korean appointment sentence into JSON:
        {"departurePlaceTitle":"...","appointmentAt":"yyyy-MM-dd'T'HH:mm:ss"}
        Rules:
        - Use relative dates (e.g., “tomorrow”) from Date.
        - Fix OCR typos to real Korean place names.
        - Bare hours (e.g., “6시”) → assume 18:00.
        - No 24‑hr notation; don’t roll times into the next day.
        """;

    private final ChatClient chat;

    @Retryable(
            retryFor = { HttpServerErrorException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public ScheduleParsedResponse parseNaturalLanguage(String userText) {
        String systemPrompt = String.format(
                SYSTEM_TMPL,
                TimeUtils.nowSeoulDate()
        );

        try {
            return chat.prompt()
                    .system(systemPrompt)
                    .user(userText)
                    .call()
                    .entity(CONVERTER);
        }
        catch (HttpClientErrorException ex) {
            throw new OpenAiParsingException();
        }
    }

    @Recover
    public ScheduleParsedResponse recover(HttpServerErrorException ex, String userText) {
        // 5xx 재시도 모두 실패했을 때
        throw new UnavailableOpenAiServerException();
    }

    @Recover
    public ScheduleParsedResponse recoverUnhandled(Throwable t, String userText) {
        throw new UnhandledOpenAiException();
    }
}
