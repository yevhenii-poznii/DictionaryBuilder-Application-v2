package com.kiskee.dictionarybuilder.exception.token;

public class ExpiredTokenException extends RuntimeException {

    public ExpiredTokenException(String message) {
        super(message);
    }
}
