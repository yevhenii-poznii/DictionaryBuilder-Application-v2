package com.kiskee.dictionarybuilder.enums.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VocabularyResponseMessageEnum {
    DICTIONARY_CREATED("%s dictionary created successfully"),
    DICTIONARY_ALREADY_EXISTS("Dictionary with name %s already exists for user"),
    DICTIONARY_UPDATED("%s dictionary updated successfully"),
    DICTIONARY_DELETED("%s dictionary deleted successfully"),
    DICTIONARIES_DELETED("Deleted %d dictionaries successfully"),
    WORD_ADDED("Word %s has been added"),
    WORD_UPDATED("Word %s has been updated"),
    WORD_FOR_REPETITION_IS_SET("Word will be used in repetition"),
    WORD_NOT_FOR_REPETITION_IS_SET("Word won't be used in repetition anymore"),
    WORD_DELETED("Word %s has been deleted"),
    WORDS_DELETE("Words %s have been deleted"),
    WORD_MOVED("Word %s has been moved to dictionary %s");

    private final String responseMessage;
}
