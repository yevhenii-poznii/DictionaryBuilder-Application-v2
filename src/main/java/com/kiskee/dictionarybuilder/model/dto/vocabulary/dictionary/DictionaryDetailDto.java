package com.kiskee.dictionarybuilder.model.dto.vocabulary.dictionary;

import com.kiskee.dictionarybuilder.repository.vocabulary.projections.DictionaryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DictionaryDetailDto implements DictionaryProjection {

    private Long id;
    private String dictionaryName;
    private int wordCount;
    private int wordsWithUseInRepetitionTrueCounter;
    private int wordsWithUseInRepetitionFalseCounter;
}
