package com.dh.ondot.schedule.infra.redis;

import com.dh.ondot.schedule.domain.PlaceHistory;
import com.dh.ondot.schedule.core.exception.SerializationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.dh.ondot.core.exception.ErrorCode.PLACE_HISTORY_SERIALIZATION_FAILED;

@Component
@RequiredArgsConstructor
public class PlaceHistoryJsonConverter {
    private final ObjectMapper objectMapper;

    public String toJson(PlaceHistory history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (JsonProcessingException e) {
            throw new SerializationException(PLACE_HISTORY_SERIALIZATION_FAILED);
        }
    }

    public PlaceHistory fromJson(String json) {
        try {
            return objectMapper.readValue(json, PlaceHistory.class);
        } catch (JsonProcessingException e) {
            throw new SerializationException(PLACE_HISTORY_SERIALIZATION_FAILED);
        }
    }
}
