package com.kiskee.vocabulary.model.dto.repetition.word;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepetitionWord {

    private Long id;
    private String word;
    private int counterRightAnswers;
}
