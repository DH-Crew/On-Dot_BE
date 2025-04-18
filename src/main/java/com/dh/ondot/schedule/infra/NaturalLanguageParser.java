package com.dh.ondot.schedule.infra;

import com.dh.ondot.schedule.api.response.ScheduleParsedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class NaturalLanguageParser {
    private static final BeanOutputConverter<ScheduleParsedResponse> CONVERTER =
            new BeanOutputConverter<>(ScheduleParsedResponse.class);

    private final ChatClient chat;

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

    public ScheduleParsedResponse parse(String userText) {
        String systemPrompt = String.format(
                SYSTEM_TMPL,
                LocalDate.now(ZoneId.of("Asia/Seoul"))
        );

        return chat.prompt()
                .system(systemPrompt)
                .user(userText)
                .call()
                .entity(CONVERTER);
    }
}
