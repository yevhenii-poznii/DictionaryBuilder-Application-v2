package com.kiskee.vocabulary.enums.registration;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum RegistrationStatus {

    USER_ALREADY_EXISTS("User with the same email or username already exists."),
    USER_SUCCESSFULLY_CREATED("Account was successfully created. " +
            "Message with link to confirm registration was sent to %s");

    private final String status;

    @Override
    public String toString() {
        return status;
    }

}
