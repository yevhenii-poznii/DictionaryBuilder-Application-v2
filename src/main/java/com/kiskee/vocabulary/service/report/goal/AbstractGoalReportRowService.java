package com.kiskee.vocabulary.service.report.goal;

import com.kiskee.vocabulary.model.dto.report.update.goal.GoalReportData;
import com.kiskee.vocabulary.model.entity.report.DictionaryGoalReport;
import com.kiskee.vocabulary.model.entity.report.ReportRow;
import com.kiskee.vocabulary.service.report.AbstractReportRowService;

public abstract class AbstractGoalReportRowService<
                D extends GoalReportData<V>, RR extends ReportRow<DR>, DR extends DictionaryGoalReport<V>, V>
        extends AbstractReportRowService<D, RR, DR> {

    protected abstract V calculateGoalForPeriod(V dailyGoal, int workingDaysForPeriod);

    protected abstract V calculateGoalForPeriod(
            int currentWorkingDays, int previousWorkingDays, V previousGoal, V goalForToday);

    protected abstract Double calculateGoalCompletionPercentage(V value, V goalForPeriod);

    protected abstract DR buildReportByDictionary(
            Long dictionaryId, Double goalCompletionPercentage, V goalForPeriod, V value);

    @Override
    protected DR calculateDictionaryReport(D reportData, int workingDays) {
        V goalForPeriod = calculateGoalForPeriod(reportData.getGoal(), workingDays);
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(reportData.getValue(), goalForPeriod);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);

        return buildReportByDictionary(
                reportData.getDictionaryId(), goalCompletionPercentage, goalForPeriod, reportData.getValue());
    }

    @Override
    protected DR recalculateDictionaryReport(RR row, int currentWorkingDays, DR dictionaryReport, D reportData) {
        Double goalCompletionPercentage = recalculateGoalCompletionPercentageForDictionaryReport(
                row, dictionaryReport, reportData, currentWorkingDays);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);
        V goalForPeriod = calculateGoalForPeriod(
                currentWorkingDays, row.getWorkingDays(), dictionaryReport.getGoalForPeriod(), reportData.getGoal());

        return !dictionaryReport.getDictionaryId().equals(reportData.getDictionaryId())
                ? dictionaryReport.buildFrom(goalCompletionPercentage, goalForPeriod)
                : dictionaryReport.buildFrom(goalCompletionPercentage, goalForPeriod, reportData.getValue());
    }

    private Double recalculateGoalCompletionPercentageForDictionaryReport(
            RR row, DR dictionaryReport, D reportData, int currentWorkingDays) {
        if (!dictionaryReport.getDictionaryId().equals(reportData.getDictionaryId())) {
            return calculateGoalCompletionPercentageWithoutNewWords(
                    dictionaryReport.getGoalCompletionPercentage(), row.getWorkingDays(), currentWorkingDays);
        }
        return calculateGoalCompletionPercentage(
                reportData.getValue(),
                reportData.getGoal(),
                dictionaryReport.getGoalCompletionPercentage(),
                row.getWorkingDays(),
                currentWorkingDays);
    }

    private Double calculateGoalCompletionPercentage(
            V value,
            V goalForToday,
            Double previousGoalCompletionPercentage,
            int previousWorkingDays,
            int currentWorkingDays) {
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(value, goalForToday);
        return ((previousGoalCompletionPercentage * previousWorkingDays) + goalCompletionPercentage)
                / currentWorkingDays;
    }

    private Double calculateGoalCompletionPercentageWithoutNewWords(
            Double previousGoalCompletionPercentage, int previousWorkingDays, int currentWorkingDays) {
        return (previousGoalCompletionPercentage * previousWorkingDays) / currentWorkingDays;
    }
}
