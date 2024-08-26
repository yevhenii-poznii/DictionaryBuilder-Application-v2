package com.kiskee.dictionarybuilder.repository.vocabulary.projections;

import java.util.Set;

public interface WordProjection {

    Long getId();

    String getWord();

    boolean getUseInRepetition();

    Set<TranslationProjection> getWordTranslations();

    String getWordHint();
}
