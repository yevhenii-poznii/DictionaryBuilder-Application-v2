package com.kiskee.dictionarybuilder.model.dto.authentication;

import jakarta.annotation.Nullable;
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
public class AuthenticationRequestDto {

    @NotBlank
    private String login;

    @NotBlank
    private String password;

    @Nullable
    private String redirectUri;

    public AuthenticationRequestDto(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
