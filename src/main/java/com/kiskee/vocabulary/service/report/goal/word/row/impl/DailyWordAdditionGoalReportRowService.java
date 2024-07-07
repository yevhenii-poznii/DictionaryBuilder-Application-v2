package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.word.period.DailyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.service.report.goal.word.row.AbstractWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Order(1)
public class DailyWordAdditionGoalReportRowService extends AbstractWordAdditionGoalReportRowService
        implements WordAdditionGoalReportRowService {

    @Override
    protected WordAdditionGoalReportRow buildPeriodRow() {
        return new DailyWordAdditionGoalReportRow();
    }

    @Override
    protected int calculateWorkingDays(LocalDate previousDay, LocalDate currentDay) {
        return 1;
    }
}
