package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_ANSWER;

public class NotFoundAnswerException extends NotFoundException {
    public NotFoundAnswerException(Long answerId) {
        super(NOT_FOUND_ANSWER.getMessage().formatted(answerId));
    }

    @Override
    public String getErrorCode() {
        return NOT_FOUND_ANSWER.name();
    }
}
