package com.dh.ondot.schedule.core.exception;

import com.dh.ondot.core.exception.BadRequestException;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOUND_MODE;

public class UnsupportedSoundModeException extends BadRequestException {
    public UnsupportedSoundModeException(String mode) {
        super(UNSUPPORTED_SOUND_MODE.getMessage().formatted(mode));
    }

    @Override
    public String getErrorCode() {
        return UNSUPPORTED_SOUND_MODE.name();
    }
}
