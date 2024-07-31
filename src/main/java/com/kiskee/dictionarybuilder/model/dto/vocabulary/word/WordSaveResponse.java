package com.kiskee.dictionarybuilder.model.dto.vocabulary.word;

import com.kiskee.dictionarybuilder.model.dto.ResponseMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class WordSaveResponse extends ResponseMessage {

    private final WordDto word;

    public WordSaveResponse(String responseMessage, WordDto word) {
        super(responseMessage);
        this.word = word;
    }
}
