package com.dh.ondot.member.core;

import com.dh.ondot.core.exception.UnsupportedException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import com.dh.ondot.member.domain.enums.OauthProvider;

import static com.dh.ondot.core.exception.ErrorCode.UNSUPPORTED_SOCIAL_LOGIN;
import static java.util.Locale.ENGLISH;

@Component
public class OauthProviderConverter implements Converter<String, OauthProvider> {
    @Override
    public OauthProvider convert(String type) {
        try {
            return OauthProvider.valueOf(type.toUpperCase(ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new UnsupportedException(UNSUPPORTED_SOCIAL_LOGIN, type);
        }
    }
}
