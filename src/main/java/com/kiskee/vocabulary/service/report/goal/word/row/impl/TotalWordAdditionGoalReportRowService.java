package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.word.period.TotalWordAdditionGoalReportRow;
import com.kiskee.vocabulary.service.report.goal.word.row.AbstractWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(2)
public class TotalWordAdditionGoalReportRowService extends AbstractWordAdditionGoalReportRowService
        implements WordAdditionGoalReportRowService {

    @Override
    protected WordAdditionGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange, Set<DictionaryWordAdditionGoalReport> dictionaryReports) {
        return new TotalWordAdditionGoalReportRow(currentPeriodRange, dictionaryReports);
    }

    @Override
    public String getRowPeriod() {
        return ReportPeriodUtil.TOTAL;
    }
}