package com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import com.kiskee.dictionarybuilder.repository.vocabulary.projections.DictionaryProjection;
import lombok.Getter;

@Getter
public class DictionarySaveResponse extends ResponseMessage {

    private final DictionaryProjection dictionary;

    public DictionarySaveResponse(String responseMessage, DictionaryProjection dictionary) {
        super(responseMessage);
        this.dictionary = dictionary;
    }
}
