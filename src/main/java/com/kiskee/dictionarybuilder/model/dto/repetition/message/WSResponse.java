package com.kiskee.dictionarybuilder.model.dto.repetition.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WSResponse {

    private String word;
    private String wordHint;
    private Long correctTranslationsCount;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;
}
