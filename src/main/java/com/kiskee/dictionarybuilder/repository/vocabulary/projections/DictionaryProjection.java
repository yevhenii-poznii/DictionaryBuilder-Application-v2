package com.kiskee.dictionarybuilder.repository.vocabulary.projections;

public interface DictionaryProjection {

    Long getId();

    String getDictionaryName();

    int getWordCount();
}
