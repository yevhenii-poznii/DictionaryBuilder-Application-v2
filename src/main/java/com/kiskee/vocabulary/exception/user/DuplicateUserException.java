package com.kiskee.vocabulary.exception.user;

import com.kiskee.vocabulary.exception.DuplicateResourceException;

public class DuplicateUserException extends DuplicateResourceException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
