package com.kiskee.dictionarybuilder.enums.token;

import com.kiskee.dictionarybuilder.util.TokenTypeConstants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TokenEnum {
    JWT_REFRESH_TOKEN(TokenTypeConstants.JWT),
    VERIFICATION_TOKEN(TokenTypeConstants.VERIFICATION_TOKEN);

    private final String value;
}
