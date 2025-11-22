package com.dh.ondot.member.domain;

import com.dh.ondot.member.domain.enums.OauthProvider;

public interface OauthApiFactory {
    OauthApi getOauthApi(OauthProvider provider);
}
