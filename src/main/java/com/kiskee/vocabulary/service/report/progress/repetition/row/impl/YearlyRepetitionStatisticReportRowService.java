package com.kiskee.vocabulary.service.report.progress.repetition.row.impl;

import com.kiskee.vocabulary.model.dto.report.update.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.YearlyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.service.report.progress.repetition.row.AbstractRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.RepetitionStatisticReportRowService;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
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
