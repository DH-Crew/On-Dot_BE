package com.dh.ondot.member.domain.enums;

import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_MAP_PROVIDER;

public enum MapProvider {
    KAKAO,
    NAVER;

    public static MapProvider from(String ringTone) {
        try {
            return MapProvider.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_MAP_PROVIDER, ringTone);
        }
    }
}
