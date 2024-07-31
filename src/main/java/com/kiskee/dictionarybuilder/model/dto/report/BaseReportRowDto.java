package com.kiskee.dictionarybuilder.model.dto.report;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseReportRowDto implements ReportRowDto {

    private LocalDate startPeriod;
    private LocalDate endPeriod;
    private int workingDays;
    private String reportPeriod;
    private List<DictionaryReportDto> dictionaryReports;
}
