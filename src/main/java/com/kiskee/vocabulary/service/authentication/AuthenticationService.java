package com.kiskee.vocabulary.service.authentication;

import com.kiskee.vocabulary.model.dto.authentication.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse issueAccessToken();

    AuthenticationResponse issueAccessToken(String refreshToken);

}
