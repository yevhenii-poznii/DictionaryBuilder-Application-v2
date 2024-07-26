package com.kiskee.vocabulary.model.entity.report;

import java.time.LocalDate;
import java.util.Set;

public interface ReportRow<DR extends DictionaryReport> {

    LocalDate getStartPeriod();

    LocalDate getEndPeriod();

    int getWorkingDays();

    Set<DR> getDictionaryReports();

    String getRowPeriod();
}
