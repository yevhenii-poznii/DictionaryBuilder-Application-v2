package com.kiskee.vocabulary.model.entity.report;

public interface DictionaryReport<V> {

    Long getDictionaryId();

    Double getGoalCompletionPercentage();

    V getGoalForPeriod();

    <DR extends DictionaryReport<V>> DR buildFrom(Double goalCompletionPercentage, V goalForPeriod, V value);

    <DR extends DictionaryReport<V>> DR buildFrom(Double goalCompletionPercentage, V goalForPeriod);
}
