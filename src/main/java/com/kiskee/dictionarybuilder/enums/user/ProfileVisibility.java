package com.kiskee.dictionarybuilder.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProfileVisibility {
    PRIVATE("Private"),
    PUBLIC("Public");

    private final String visibility;
}
