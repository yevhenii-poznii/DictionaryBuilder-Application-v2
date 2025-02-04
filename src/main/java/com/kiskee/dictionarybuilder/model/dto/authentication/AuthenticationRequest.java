package com.kiskee.dictionarybuilder.model.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class AuthenticationRequest {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    private String redirectUri;
}
