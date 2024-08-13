package com.kiskee.dictionarybuilder.service.authentication;

public interface LogoutService {

    void revokeRefreshToken(String refreshToken);
}
