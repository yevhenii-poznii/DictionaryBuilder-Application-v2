package com.kiskee.dictionarybuilder.model.dto.authentication;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private Instant expirationTime;
}
