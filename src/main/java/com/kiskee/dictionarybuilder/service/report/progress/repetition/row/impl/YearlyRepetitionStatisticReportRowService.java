package com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.YearlyRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.AbstractRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.RepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class YearlyRepetitionStatisticReportRowService extends AbstractRepetitionStatisticReportRowService
        implements RepetitionStatisticReportRowService {

    @Override
    protected RepetitionStatisticReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionStatisticReport> dictionaryReports) {
        return new YearlyRepetitionStatisticReportRow(currentPeriodRange, workingDaysForPeriod, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.YEAR;
    }
}
