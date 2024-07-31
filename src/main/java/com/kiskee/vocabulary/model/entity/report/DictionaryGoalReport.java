package com.kiskee.vocabulary.model.entity.report;

public interface DictionaryGoalReport<V> extends DictionaryReport {

    Double getGoalCompletionPercentage();

    V getGoalForPeriod();

    <DR extends DictionaryGoalReport<V>> DR buildFrom(
            String dictionaryName, Double goalCompletionPercentage, V goalForPeriod, V value);

    <DR extends DictionaryGoalReport<V>> DR buildFrom(Double goalCompletionPercentage, V goalForPeriod);
}
