package com.kiskee.vocabulary.model.dto.vocabulary.word;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WordDto {

    private Long id;
    private String word;
    private boolean useInRepetition;
    private List<WordTranslationDto> wordTranslations;
    private WordHintDto wordHint;

}
