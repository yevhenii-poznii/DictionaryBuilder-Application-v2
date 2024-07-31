package com.kiskee.dictionarybuilder.service.report.goal.word.row;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.dto.report.update.goal.WordAdditionData;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.dictionarybuilder.service.report.goal.AbstractGoalReportRowService;
import java.util.Set;

public abstract class AbstractWordAdditionGoalReportRowService
        extends AbstractGoalReportRowService<
                WordAdditionData, WordAdditionGoalReportRow, DictionaryWordAdditionGoalReport, Integer> {

    @Override
    protected Integer calculateGoalForPeriod(Integer dailyGoal, int workingDaysForPeriod) {
        return dailyGoal * workingDaysForPeriod;
    }

    @Override
    protected Integer calculateGoalForPeriod(
            int currentWorkingDays, int previousWorkingDays, Integer previousGoal, Integer goalForToday) {
        if (previousGoal == 0) {
            return calculateGoalForPeriod(goalForToday, currentWorkingDays);
        }
        int currentWorkingDaysWithoutCurrentDay = currentWorkingDays - 1; // excluding current day
        double previousDailyAverageWordsGoal = (double) previousGoal / previousWorkingDays;
        double previousWordsGoalForPeriod = previousDailyAverageWordsGoal * currentWorkingDaysWithoutCurrentDay;
        return (int) (previousWordsGoalForPeriod + goalForToday);
    }

    @Override
    protected Double calculateGoalCompletionPercentage(Integer value, Integer goalForPeriod) {
        return ((double) value / goalForPeriod) * 100;
    }

    @Override
    protected DictionaryWordAdditionGoalReport buildReportByDictionary(
            WordAdditionData wordAdditionData, Double goalCompletionPercentage, Integer goalForPeriod) {
        return new DictionaryWordAdditionGoalReport(
                wordAdditionData.getDictionaryId(),
                wordAdditionData.getDictionaryName(),
                goalCompletionPercentage,
                goalForPeriod,
                wordAdditionData.getValue());
    }

    @Override
    protected DictionaryWordAdditionGoalReport buildReportByDictionary(Long dictionaryId) {
        return new DictionaryWordAdditionGoalReport(dictionaryId);
    }

    @Override
    protected WordAdditionGoalReportRow rebuildRow(
            WordAdditionGoalReportRow row,
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryWordAdditionGoalReport> recalculatedDictionaryReports) {
        return row.toBuilder()
                .reportPeriod(row.getRowPeriod())
                .startPeriod(currentPeriodRange.startPeriod())
                .endPeriod(currentPeriodRange.endPeriod())
                .workingDays(workingDaysForPeriod)
                .dictionaryReports(recalculatedDictionaryReports)
                .build();
    }
}
