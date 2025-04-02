package com.dh.ondot.member.domain;

public interface OauthApiFactory {
    OauthApi getOauthApi(OauthProvider provider);
}
