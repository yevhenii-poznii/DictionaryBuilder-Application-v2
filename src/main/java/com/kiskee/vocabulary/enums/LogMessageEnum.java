package com.kiskee.vocabulary.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogMessageEnum {

    WORD_ADDED("Word [{}] has been added in dictionary [{}] for [{}]"),
    WORD_UPDATED("Word [{}] has been updated in dictionary [{}] for [{}]"),
    WORD_DELETED("Word [{}] has been deleted from dictionary [{}] for [{}]");

    private final String message;

}
