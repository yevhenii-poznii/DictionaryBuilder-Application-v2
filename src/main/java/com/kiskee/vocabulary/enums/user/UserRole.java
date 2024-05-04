package com.kiskee.vocabulary.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_METRICS("ROLE_METRICS"),
    ROLE_USER("ROLE_USER");

    private final String role;
}
