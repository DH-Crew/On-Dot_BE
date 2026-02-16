package com.dh.ondot.schedule.core

import com.dh.ondot.core.exception.ErrorCode.EVENT_SERIALIZATION_FAILED
import com.dh.ondot.schedule.core.exception.SerializationException
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JacksonEventSerializer(
    private val mapper: ObjectMapper,
) : EventSerializer {

    override fun serialize(event: Any): String =
        try {
            mapper.writeValueAsString(event)
        } catch (e: JsonProcessingException) {
            throw SerializationException(EVENT_SERIALIZATION_FAILED)
        }
}
