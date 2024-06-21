package com.kiskee.vocabulary.model.dto.vocabulary.word;

import java.util.List;
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
    private List<WordTranslationDto> wordTranslations;
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
