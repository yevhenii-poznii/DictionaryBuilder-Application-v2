package com.kiskee.vocabulary.enums.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageFilter {
    BY_ADDED_AT_ASC("addedAtASC"),
    BY_ADDED_AT_DESC("addedAtDESC"),
    ONLY_USE_IN_REPETITION_ASC("onlyUseInRepetitionASC"),
    ONLY_USE_IN_REPETITION_DESC("onlyUseInRepetitionDESC"),
    ONLY_NOT_USE_IN_REPETITION_ASC("onlyNotUseInRepetitionASC"),
    ONLY_NOT_USE_IN_REPETITION_DESC("onlyNotUseInRepetitionDESC"),
    LATEST_BY_DATE_ASC("latestByDateASC"),
    LATEST_BY_DATE_DESC("latestByDateDESC"),
    LATEST_BY_WEEK_ASC("latestByWeekASC"),
    LATEST_BY_WEEK_DESC("latestByWeekDESC"),
    LATEST_BY_MONTH_ASC("latestByMonthASC"),
    LATEST_BY_MONTH_DESC("latestByMonthDESC"),
    LATEST_BY_YEAR_ASC("latestByYearASC"),
    LATEST_BY_YEAR_DESC("latestByYearDESC");

    private final String filter;
}
