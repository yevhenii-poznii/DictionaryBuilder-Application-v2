package com.kiskee.dictionarybuilder.enums.vocabulary.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageFilter implements WordFilter {
    BY_ADDED_AT_ASC("Oldest First"),
    BY_ADDED_AT_DESC("Newest First"),
    ONLY_USE_IN_REPETITION_ASC("In Repetition (A-Z)"),
    ONLY_USE_IN_REPETITION_DESC("In Repetition (Z-A)"),
    ONLY_NOT_USE_IN_REPETITION_ASC("Not in Repetition (A-Z)"),
    ONLY_NOT_USE_IN_REPETITION_DESC("Not in Repetition (Z-A)");

    private final String filter;
}
