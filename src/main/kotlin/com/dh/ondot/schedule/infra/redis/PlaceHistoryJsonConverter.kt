package com.dh.ondot.schedule.infra.redis

import com.dh.ondot.core.exception.ErrorCode.PLACE_HISTORY_SERIALIZATION_FAILED
import com.dh.ondot.schedule.core.exception.SerializationException
import com.dh.ondot.schedule.domain.PlaceHistory
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class PlaceHistoryJsonConverter(
    private val objectMapper: ObjectMapper,
) {
    fun toJson(history: PlaceHistory): String {
        try {
            return objectMapper.writeValueAsString(history)
        } catch (e: JsonProcessingException) {
            throw SerializationException(PLACE_HISTORY_SERIALIZATION_FAILED)
        }
    }

    fun fromJson(json: String): PlaceHistory {
        try {
            return objectMapper.readValue(json, PlaceHistory::class.java)
        } catch (e: JsonProcessingException) {
            throw SerializationException(PLACE_HISTORY_SERIALIZATION_FAILED)
        }
    }
}
