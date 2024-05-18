package com.kiskee.vocabulary.repository.vocabulary.projections;

public interface DictionaryProjection {

    Long getId();

    String getDictionaryName();

    int getWordCount();
}
