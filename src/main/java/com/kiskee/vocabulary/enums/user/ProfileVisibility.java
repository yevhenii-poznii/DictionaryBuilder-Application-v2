package com.kiskee.vocabulary.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileVisibility {

    PRIVATE("private"),
    PUBLIC_FOR_FRIEND("public_for_friend"),
    PUBLIC("public");

    private final String profileVisibility;

}
