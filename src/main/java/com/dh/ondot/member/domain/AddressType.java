package com.dh.ondot.member.domain;

import com.dh.ondot.member.core.exception.UnsupportedAddressTypeException;

import java.util.Locale;

public enum AddressType {
    HOME
    ;

    public static AddressType from(String ringTone) {
        try {
            return AddressType.valueOf(ringTone.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedAddressTypeException(ringTone);
        }
    }
}
