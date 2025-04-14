package com.dh.ondot.member.domain;

import com.dh.ondot.member.core.exception.UnsupportedMapProviderException;

import java.util.Locale;

public enum MapProvider {
    KAKAO,
    NAVER;

    public static MapProvider from(String ringTone) {
        try {
            return MapProvider.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedMapProviderException(ringTone);
        }
    }
}
