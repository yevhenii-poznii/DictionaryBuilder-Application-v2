package com.kiskee.vocabulary.model.dto.vocabulary.word;

import com.kiskee.vocabulary.model.dto.ResponseMessage;
import lombok.Getter;

@Getter
public class WordSaveResponse extends ResponseMessage {

    private final WordDto word;

    public WordSaveResponse(String responseMessage, WordDto word) {
        super(responseMessage);
        this.word = word;
    }

}
