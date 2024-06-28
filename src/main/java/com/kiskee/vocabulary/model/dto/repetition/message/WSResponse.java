package com.kiskee.vocabulary.model.dto.repetition.message;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WSResponse {

    private String word;
    private String wordHint;
    private Boolean previousAnswerIsCorrect;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int totalElements;
    private int totalElementsPassed;
}
