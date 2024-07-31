package com.kiskee.dictionarybuilder.service.report.goal.time.row.impl;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.TotalRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.AbstractRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
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
