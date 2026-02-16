package com.dh.ondot.member.core.exception

import com.dh.ondot.core.exception.ErrorCode.OAUTH_USER_FETCH_FAILED
import com.dh.ondot.core.exception.ServiceUnavailableException

class OauthUserFetchFailedException(oauthProvider: String) :
    ServiceUnavailableException(OAUTH_USER_FETCH_FAILED.message.format(oauthProvider)) {
    override val errorCode: String get() = OAUTH_USER_FETCH_FAILED.name
}
