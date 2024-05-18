package com.kiskee.vocabulary.enums.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VocabularyResponseMessageEnum {
    DICTIONARY_CREATED("%s dictionary created successfully"),
    DICTIONARY_ALREADY_EXISTS("Dictionary with name %s already exists for user"),
    DICTIONARY_UPDATED("%s dictionary updated successfully"),
    DICTIONARY_DELETED("%s dictionary deleted successfully"),
    WORD_ADDED("Word %s has been added"),
    WORD_UPDATED("Word %s has been updated"),
    WORD_DELETED("Word %s has been deleted");

    private final String responseMessage;
}
