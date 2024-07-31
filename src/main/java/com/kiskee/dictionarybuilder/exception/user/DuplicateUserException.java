package com.kiskee.dictionarybuilder.exception.user;

import com.kiskee.dictionarybuilder.exception.DuplicateResourceException;

public class DuplicateUserException extends DuplicateResourceException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
