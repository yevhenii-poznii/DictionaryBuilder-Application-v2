package com.kiskee.dictionarybuilder.exception;

public class ForbiddenAccessException extends RuntimeException {

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
