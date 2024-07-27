package com.kiskee.vocabulary.service.report.goal.time.row.impl;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.MonthlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.service.report.goal.time.row.AbstractRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class MonthlyRepetitionTimeSpendGoalReportRowService extends AbstractRepetitionTimeSpendGoalReportRowService
        implements RepetitionTimeSpendGoalReportRowService {

    @Override
    protected RepetitionTimeSpendGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionTimeSpendGoalReport> dictionaryReports) {
        return new MonthlyRepetitionTimeSpendGoalReportRow(currentPeriodRange, workingDaysForPeriod, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.MONTH;
    }
}
