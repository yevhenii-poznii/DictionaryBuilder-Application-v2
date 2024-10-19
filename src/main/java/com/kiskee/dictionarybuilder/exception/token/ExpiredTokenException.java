package com.kiskee.dictionarybuilder.exception.token;

import com.kiskee.dictionarybuilder.model.dto.token.TokenData;
import lombok.Getter;

@Getter
public class ExpiredTokenException extends Exception {

    private Class<? extends TokenData> tokenDataClass;

    public ExpiredTokenException(String message) {
        super(message);
    }

    public ExpiredTokenException(String message, Class<? extends TokenData> tokenDataClass) {
        super(message);
        this.tokenDataClass = tokenDataClass;
    }
}
