package com.kiskee.vocabulary.enums.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageFilter {
    BY_ADDED_AT_ASC("addedAtASC"),
    BY_ADDED_AT_DESC("addedAtDESC"),
    ONLY_USE_IN_REPETITION_ASC("onlyUseInRepetitionASC"),
    ONLY_USE_IN_REPETITION_DESC("onlyUseInRepetitionDESC"),
    ONLY_NOT_USE_IN_REPETITION_ASC("onlyNotUseInRepetitionASC"),
    ONLY_NOT_USE_IN_REPETITION_DESC("onlyNotUseInRepetitionDESC");

    private final String filter;
}
