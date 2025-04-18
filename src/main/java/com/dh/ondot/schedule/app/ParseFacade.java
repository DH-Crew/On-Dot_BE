package com.dh.ondot.schedule.app;

import com.dh.ondot.schedule.api.response.ScheduleParsedResponse;
import com.dh.ondot.schedule.infra.NaturalLanguageParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParseFacade {
    private final NaturalLanguageParser parser;

    public ScheduleParsedResponse parse(String sentence) {
        return parser.parse(sentence);
    }
}
