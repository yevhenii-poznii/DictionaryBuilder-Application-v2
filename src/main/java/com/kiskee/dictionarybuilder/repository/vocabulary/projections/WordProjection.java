package com.kiskee.dictionarybuilder.repository.vocabulary.projections;

import java.util.Set;

public interface WordProjection {

    Long getId();

    String getWord();

    boolean isUseInRepetition();

    Set<?> getWordTranslations();

    String getWordHint();
}
