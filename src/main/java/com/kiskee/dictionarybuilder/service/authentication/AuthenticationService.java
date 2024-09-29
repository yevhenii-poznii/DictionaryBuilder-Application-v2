package com.kiskee.dictionarybuilder.service.authentication;

import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.TokenData;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    AuthenticationResponse issueAccessToken();

    AuthenticationResponse issueAccessToken(String refreshToken);

    TokenData issueRefreshToken(Authentication authentication);
}
