package com.dh.ondot.member.core.exception;

import com.dh.ondot.core.exception.ServiceUnavailableException;

import static com.dh.ondot.core.exception.ErrorCode.OAUTH_USER_FETCH_FAILED;

public class OauthUserFetchFailedException extends ServiceUnavailableException {
    public OauthUserFetchFailedException(String oauthProvider) {
        super(OAUTH_USER_FETCH_FAILED.getMessage().formatted(oauthProvider));
    }

    @Override
    public String getErrorCode() {
        return OAUTH_USER_FETCH_FAILED.name();
    }
}
