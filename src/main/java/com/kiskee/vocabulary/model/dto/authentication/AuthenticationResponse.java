package com.kiskee.vocabulary.model.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private Instant expirationTime;

}
