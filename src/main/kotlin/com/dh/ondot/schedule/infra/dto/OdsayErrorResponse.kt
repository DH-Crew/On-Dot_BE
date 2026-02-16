package com.dh.ondot.schedule.infra.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

data class OdsayErrorResponse(
    @param:JsonFormat(with = [JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY])
    val error: List<Error>,
) {
    data class Error(
        val code: String,
        val message: String,
    ) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun create(
                @JsonProperty("code") code: String,
                @JsonProperty("message") message: String?,
                @JsonProperty("msg") msg: String?,
            ): Error = Error(code, message ?: msg ?: "")
        }
    }
}
