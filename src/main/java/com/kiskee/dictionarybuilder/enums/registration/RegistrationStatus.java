package com.kiskee.dictionarybuilder.enums.registration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RegistrationStatus {
    USER_ALREADY_EXISTS("User with the same email or username already exists."),
    USER_SUCCESSFULLY_CREATED(
            "Account was successfully created. " + "Message with link to confirm registration was sent to %s"),
    USER_SUCCESSFULLY_ACTIVATED("Your account has been successfully activated. Now you can sign in your account.");

    private final String status;
}
