package com.kiskee.dictionarybuilder.service.report.goal.time.row;

import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.service.report.goal.AbstractGoalReportRowService;
import java.time.Duration;
import java.util.Set;

public abstract class AbstractRepetitionTimeSpendGoalReportRowService
        extends AbstractGoalReportRowService<
                RepetitionTimeSpendData,
                RepetitionTimeSpendGoalReportRow,
                DictionaryRepetitionTimeSpendGoalReport,
                Duration> {

    @Override
    protected Duration calculateGoalForPeriod(Duration dailyGoal, int workingDaysForPeriod) {
        return dailyGoal.multipliedBy(workingDaysForPeriod);
    }

    @Override
    protected Duration calculateGoalForPeriod(
            int currentWorkingDays, int previousWorkingDays, Duration previousGoal, Duration goalForToday) {
        if (previousGoal.isZero()) {
            return calculateGoalForPeriod(goalForToday, currentWorkingDays);
        }
        int currentWorkingDaysWithoutCurrentDay = currentWorkingDays - 1; // excluding current day
        Duration previousDailyAverageWordsGoal = previousGoal.dividedBy(previousWorkingDays);
        Duration previousWordsGoalForPeriod =
                previousDailyAverageWordsGoal.multipliedBy(currentWorkingDaysWithoutCurrentDay);
        return previousWordsGoalForPeriod.plus(goalForToday);
    }

    @Override
    protected Double calculateGoalCompletionPercentage(Duration value, Duration goalForPeriod) {
        double valueInSeconds = value.toSeconds();
        double goalInSeconds = goalForPeriod.toSeconds();
        return (valueInSeconds / goalInSeconds) * 100;
    }

    @Override
    protected DictionaryRepetitionTimeSpendGoalReport buildReportByDictionary(
            RepetitionTimeSpendData repetitionTimeSpendData, Double goalCompletionPercentage, Duration goalForPeriod) {
        return new DictionaryRepetitionTimeSpendGoalReport(
                repetitionTimeSpendData.getDictionaryId(),
                repetitionTimeSpendData.getDictionaryName(),
                goalCompletionPercentage,
                goalForPeriod,
                repetitionTimeSpendData.getValue(),
                1);
    }

    @Override
    protected DictionaryRepetitionTimeSpendGoalReport buildReportByDictionary(Long dictionaryId) {
        return new DictionaryRepetitionTimeSpendGoalReport(dictionaryId);
    }

    @Override
    protected RepetitionTimeSpendGoalReportRow rebuildRow(
            RepetitionTimeSpendGoalReportRow row,
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryRepetitionTimeSpendGoalReport> recalculatedDictionaryReports) {
        return row.toBuilder()
                .reportPeriod(row.getRowPeriod())
                .startPeriod(currentPeriodRange.startPeriod())
                .endPeriod(currentPeriodRange.endPeriod())
                .workingDays(workingDaysForPeriod)
                .dictionaryReports(recalculatedDictionaryReports)
                .build();
    }
}
