package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.NotFoundException;

import static com.dh.ondot.core.exception.ErrorCode.NOT_FOUND_QUESTION;

public class NotFoundQuestionException extends NotFoundException {
    public NotFoundQuestionException(Long questionId) {
        super(NOT_FOUND_QUESTION.getMessage().formatted(questionId));
    }

  @Override
  public String getErrorCode() {
      return NOT_FOUND_QUESTION.name();
  }
}
