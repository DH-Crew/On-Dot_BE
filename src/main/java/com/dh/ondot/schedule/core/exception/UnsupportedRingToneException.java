package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_RING_TONE;

public class UnsupportedRingToneException extends BadRequestException {
    public UnsupportedRingToneException(String ringTone) {
        super(UNSUPPORTED_RING_TONE.getMessage().formatted(ringTone));
    }

    @Override
    public String getErrorCode() {
        return UNSUPPORTED_RING_TONE.name();
    }
}
