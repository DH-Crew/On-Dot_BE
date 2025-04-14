package com.dh.ondot.member.domain.enums;

import com.dh.ondot.core.exception.UnsupportedException;

import java.util.Locale;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_ADDRESS_TYPE;

public enum AddressType {
    HOME;

    public static AddressType from(String type) {
        try {
            return AddressType.valueOf(type.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_ADDRESS_TYPE, type);
        }
    }
}
