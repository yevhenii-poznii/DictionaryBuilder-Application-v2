package com.kiskee.vocabulary.enums.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VocabularyResponseMessageEnum {

    DICTIONARY_CREATED("%s dictionary created successfully"),
    DICTIONARY_ALREADY_EXISTS("Dictionary with name %s already exists for user");

    private final String responseMessage;

}
