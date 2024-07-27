package com.kiskee.vocabulary.service.report.goal.time.row;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.dto.report.goal.RepetitionTimeSpendData;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.service.report.AbstractReportRowService;
import java.time.Duration;
import java.util.Set;

public abstract class AbstractRepetitionTimeSpendGoalReportRowService
        extends AbstractReportRowService<
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
            Long dictionaryId, Double goalCompletionPercentage, Duration goalForPeriod, Duration value) {
        return new DictionaryRepetitionTimeSpendGoalReport(
                dictionaryId, goalCompletionPercentage, goalForPeriod, value, 1);
    }

    @Override
    protected DictionaryRepetitionTimeSpendGoalReport buildReportByDictionary(Long dictionaryId) {
        return new DictionaryRepetitionTimeSpendGoalReport(dictionaryId, 0.0, Duration.ZERO, Duration.ZERO, 0);
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
