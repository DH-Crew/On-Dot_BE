package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.OPEN_AI_PARSING_ERROR;

public class OpenAiParsingException extends BadRequestException {
    public OpenAiParsingException() {
        super(OPEN_AI_PARSING_ERROR.getMessage());
    }

    @Override
    public String getErrorCode() {
        return OPEN_AI_PARSING_ERROR.name();
    }
}
