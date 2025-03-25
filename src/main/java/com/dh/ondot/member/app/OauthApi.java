package com.dh.ondot.member.app;

import com.dh.ondot.member.api.response.UserInfo;

public interface OauthApi {
    String getAccessToken(String code);
    UserInfo getOauthUser(String accessToken);
}