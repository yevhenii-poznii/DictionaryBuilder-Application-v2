package com.kiskee.dictionarybuilder.service.report.goal.word.row.impl;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.period.MonthlyWordAdditionGoalReportRow;
import com.kiskee.dictionarybuilder.service.report.goal.word.row.AbstractWordAdditionGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class MonthlyWordAdditionGoalReportRowService extends AbstractWordAdditionGoalReportRowService
        implements WordAdditionGoalReportRowService {

    @Override
    protected WordAdditionGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        return new MonthlyWordAdditionGoalReportRow(currentPeriodRange, workingDaysForPeriod, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.MONTH;
    }
}
