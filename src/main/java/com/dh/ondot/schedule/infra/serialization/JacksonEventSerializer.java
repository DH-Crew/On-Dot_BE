package com.dh.ondot.schedule.infra.serialization;

import com.dh.ondot.schedule.application.port.EventSerializer;
import com.dh.ondot.schedule.core.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import static com.dh.ondot.core.exception.ErrorCode.EVENT_SERIALIZATION_FAILED;

@Component
public class JacksonEventSerializer implements EventSerializer {
    private final ObjectMapper mapper;

    public JacksonEventSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String serialize(Object event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new SerializationException(EVENT_SERIALIZATION_FAILED);
        }
    }
}
