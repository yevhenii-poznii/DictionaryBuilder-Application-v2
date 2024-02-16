package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;
import com.kiskee.vocabulary.model.dto.token.TokenData;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    AuthenticationResponse issueAccessToken();

    AuthenticationResponse issueAccessToken(String refreshToken);

    TokenData issueRefreshToken(Authentication authentication);

}
