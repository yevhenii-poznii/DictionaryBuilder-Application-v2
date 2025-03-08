package com.kiskee.dictionarybuilder.model.dto.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String redirectUri;

    public AuthenticationResponse(String token, Instant expirationTime) {
        this.token = token;
        this.expirationTime = expirationTime;
    }
}
