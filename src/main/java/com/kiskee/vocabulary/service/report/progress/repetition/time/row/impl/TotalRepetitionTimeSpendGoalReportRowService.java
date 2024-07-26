package com.kiskee.vocabulary.service.report.progress.repetition.time.row.impl;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.TotalRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.service.report.progress.repetition.time.row.AbstractRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class TotalRepetitionTimeSpendGoalReportRowService extends AbstractRepetitionTimeSpendGoalReportRowService
        implements RepetitionTimeSpendGoalReportRowService {

    @Override
    protected RepetitionTimeSpendGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionTimeSpendGoalReport> dictionaryReports) {
        return new TotalRepetitionTimeSpendGoalReportRow(currentPeriodRange, workingDaysForPeriod, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.TOTAL;
    }
}
