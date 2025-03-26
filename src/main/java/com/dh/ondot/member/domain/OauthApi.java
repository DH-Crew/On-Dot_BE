package com.dh.ondot.member.domain;

import com.dh.ondot.member.domain.dto.UserInfo;

public interface OauthApi {
    UserInfo fetchUser(String accessToken);
}
