package com.kiskee.vocabulary.service.report.goal.time.row.impl;

import com.kiskee.vocabulary.model.dto.report.update.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.YearlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.service.report.goal.time.row.AbstractRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class YearlyRepetitionTimeSpendGoalReportRowService extends AbstractRepetitionTimeSpendGoalReportRowService
        implements RepetitionTimeSpendGoalReportRowService {

    @Override
    protected RepetitionTimeSpendGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionTimeSpendGoalReport> dictionaryReports) {
        return new YearlyRepetitionTimeSpendGoalReportRow(currentPeriodRange, workingDaysForPeriod, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.YEAR;
    }
}
