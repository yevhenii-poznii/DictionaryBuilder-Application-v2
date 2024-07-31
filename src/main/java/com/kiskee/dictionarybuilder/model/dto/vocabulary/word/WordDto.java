package com.kiskee.dictionarybuilder.model.dto.vocabulary.word;

import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WordDto {

    private Long id;
    private String word;
    private boolean useInRepetition;
    private Set<WordTranslationDto> wordTranslations;
    private int counterRightAnswers;
    private String wordHint;

    public void incrementCounterRightAnswers() {
        this.counterRightAnswers++;
    }

    public void decrementCounterRightAnswers() {
        if (this.counterRightAnswers > 0) {
            this.counterRightAnswers--;
        }
    }
}
