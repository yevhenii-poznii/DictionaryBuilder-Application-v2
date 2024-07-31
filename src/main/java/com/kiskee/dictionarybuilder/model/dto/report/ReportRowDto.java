package com.kiskee.dictionarybuilder.model.dto.report;

import java.time.LocalDate;
import java.util.List;

public interface ReportRowDto {

    LocalDate getStartPeriod();

    LocalDate getEndPeriod();

    int getWorkingDays();

    String getReportPeriod();

    List<DictionaryReportDto> getDictionaryReports();
}
