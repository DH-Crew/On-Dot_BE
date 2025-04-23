package com.dh.ondot.schedule.infra.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OdsayErrorResponse(
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<Error> error
) {
    public record Error(
            String code,
            String message
    ) {
        @JsonCreator
        public Error(@JsonProperty("code") String code,
                     @JsonProperty("message") String message,
                     @JsonProperty("msg") String msg) {
            this(code, message != null ? message : msg);
        }
    }
}
