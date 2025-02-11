package com.kiskee.dictionarybuilder.service.authentication;

import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationRequestDto;
import com.kiskee.dictionarybuilder.model.dto.authentication.AuthenticationResponse;
import com.kiskee.dictionarybuilder.model.dto.token.jwe.JweTokenData;
import org.springframework.security.core.Authentication;

public interface AuthenticationService {

    AuthenticationResponse issueAccessToken(AuthenticationRequestDto authenticationRequestDto);

    AuthenticationResponse issueAccessToken(String refreshToken);

    JweTokenData issueRefreshToken(Authentication authentication);
}
