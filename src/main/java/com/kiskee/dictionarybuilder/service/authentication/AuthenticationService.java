package com.kiskee.dictionarybuilder.service.authentication;

import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationRequest;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    AuthenticationResponse issueAccessToken(AuthenticationRequest authenticationRequest);

    AuthenticationResponse issueAccessToken(String refreshToken);

    JweTokenData issueRefreshToken(Authentication authentication);
}
