package com.kiskee.vocabulary.model.dto.vocabulary.dictionary;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import com.kiskee.vocabulary.repository.vocabulary.projections.DictionaryProjection;
import lombok.Getter;

@Getter
public class DictionarySaveResponse extends ResponseMessage {

    private final DictionaryProjection dictionary;

    public DictionarySaveResponse(String responseMessage, DictionaryProjection dictionary) {
        super(responseMessage);
        this.dictionary = dictionary;
    }

}
