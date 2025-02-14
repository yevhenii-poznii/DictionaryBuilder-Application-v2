package com.kiskee.dictionarybuilder.model.dto.authentication;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private Instant expirationTime;

    @Setter
    private String redirectUri;

    public AuthenticationResponse(String token, Instant expirationTime) {
        this.token = token;
        this.expirationTime = expirationTime;
    }
}
