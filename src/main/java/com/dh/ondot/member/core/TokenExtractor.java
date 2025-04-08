package com.dh.ondot.member.core;

import com.dh.ondot.member.core.exception.TokenMissingException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenExtractor {
    private static final String BEARER_PREFIX = "Bearer ";

    public static String extract(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new TokenMissingException();
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
