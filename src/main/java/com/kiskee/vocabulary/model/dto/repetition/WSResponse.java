package com.kiskee.vocabulary.model.dto.repetition;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WSResponse {

    private String word;
    private Boolean previousAnswerIsCorrect;
}
