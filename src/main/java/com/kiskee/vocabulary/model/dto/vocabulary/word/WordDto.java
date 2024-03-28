package com.kiskee.vocabulary.model.dto.vocabulary.word;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class WordDto {

    private Long id;
    private String word;
    private boolean useInRepetition;
    private List<WordTranslationDto> wordTranslations;
    private String wordHint;

}
