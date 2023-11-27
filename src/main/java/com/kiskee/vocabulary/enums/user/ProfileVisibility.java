package com.kiskee.vocabulary.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileVisibility {

    PRIVATE("private"),
    PUBLIC("public");

    private final String profileVisibility;

}
