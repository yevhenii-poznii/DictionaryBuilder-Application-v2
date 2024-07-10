package com.kiskee.vocabulary.service.report.goal.word.row;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractWordAdditionGoalReportRowService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    public abstract String getRowPeriod();

    protected abstract WordAdditionGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange, Set<DictionaryWordAdditionGoalReport> dictionaryReports);

    public WordAdditionGoalReportRow buildRowFromScratch(WordAdditionData wordAdditionData) {
        PeriodRange currentPeriodRange =
                buildPeriodRange(wordAdditionData.currentDate(), wordAdditionData.userCreatedAt());
//
//        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);
//
//        int newWordsGoalForPeriod = wordAdditionData.newWordsPerDayGoal() * workingDaysForPeriod;
//
//        Double goalCompletionPercentage = calculateGoalCompletionPercentage(1, newWordsGoalForPeriod);
//
//        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);

        CalculationResultData calculationResultData = calculate(
                currentPeriodRange,
                wordAdditionData,
                () -> calculateGoalCompletionPercentage(1, wordAdditionData.newWordsPerDayGoal())
        );

        DictionaryWordAdditionGoalReport reportByDictionary = buildReportByDictionary(
                wordAdditionData.dictionaryId(), calculationResultData.goalCompletionPercentage(),
                calculationResultData.newWordsGoalForPeriod());

        return buildPeriodRow(calculationResultData.currentPeriodRange(), Set.of(reportByDictionary));
    }

    private CalculationResultData calculate(PeriodRange currentPeriodRange, WordAdditionData wordAdditionData,
                                            Supplier<Double> goalCompletionPercentageSupplier) {
        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);

        int newWordsGoalForPeriod = wordAdditionData.newWordsPerDayGoal() * workingDaysForPeriod;

        Double goalCompletionPercentage = goalCompletionPercentageSupplier.get();

        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);

        return new CalculationResultData(currentPeriodRange, goalCompletionPercentage, newWordsGoalForPeriod);
    }

    public WordAdditionGoalReportRow updateRow(WordAdditionGoalReportRow row, WordAdditionData wordAdditionData) {
        String rowPeriod = getRowPeriod();
        LocalDate lastDayOfPeriod = ReportPeriodUtil.getLastDayOfPeriod(row.getEndPeriod(), rowPeriod);
        if (wordAdditionData.currentDate().isAfter(lastDayOfPeriod) && !rowPeriod.equals(ReportPeriodUtil.TOTAL)) {
            return buildRowFromScratch(wordAdditionData);
        }

        PeriodRange currentPeriodRange =
                buildPeriodRange(wordAdditionData.currentDate(), wordAdditionData.userCreatedAt());

        Set<DictionaryWordAdditionGoalReport> recalculatedDictionaryReports = row.getDictionaryReports().stream()
                .map(dictionaryReport -> recalculateDictionaryReport(currentPeriodRange, dictionaryReport, wordAdditionData))
                .collect(Collectors.toSet());

        return buildPeriodRow(currentPeriodRange, recalculatedDictionaryReports);
    }

    private DictionaryWordAdditionGoalReport recalculateDictionaryReport(PeriodRange currentPeriodRange,
                                                                         DictionaryWordAdditionGoalReport dictionaryReport, WordAdditionData wordAdditionData) {
        if (!dictionaryReport.getDictionaryId().equals(wordAdditionData.dictionaryId())) {
            return dictionaryReport;
        }

//        PeriodRange currentPeriodRange =
//                buildPeriodRange(wordAdditionData.currentDate(), wordAdditionData.userCreatedAt());
//
//        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);
//
//        int newWordsGoalForPeriod = wordAdditionData.newWordsPerDayGoal() * workingDaysForPeriod;
//
//        Double goalCompletionPercentage =
//                calculateGoalCompletionPercentage(1, dictionaryReport.getNewWordsActual(), newWordsGoalForPeriod);
//
//        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);
        CalculationResultData calculationResultData = calculate(
                currentPeriodRange,
                wordAdditionData,
                () -> calculateGoalCompletionPercentage(
                        1, dictionaryReport.getNewWordsActual(), wordAdditionData.newWordsPerDayGoal())
        );

        return dictionaryReport.buildFrom(
                calculationResultData.goalCompletionPercentage(), calculationResultData.newWordsGoalForPeriod());
    }

    protected PeriodRange buildPeriodRange(LocalDate currentDate, LocalDate userCreatedAt) {
        String rowPeriod = getRowPeriod();
        PeriodRange currentPeriodRange = ReportPeriodUtil.getCurrentPeriodRange(currentDate, rowPeriod);

        return userWasCreatedBeforeStartPeriod(currentPeriodRange.startPeriod(), userCreatedAt)
                ? currentPeriodRange
                : new PeriodRange(userCreatedAt, currentDate);
    }

    protected boolean userWasCreatedBeforeStartPeriod(LocalDate startPeriod, LocalDate userCreatedAt) {
        return userCreatedAt.isBefore(startPeriod) && !getRowPeriod().equals(ReportPeriodUtil.TOTAL);
    }

    protected int calculateWorkingDaysForPeriod(PeriodRange currentPeriodRange) {
        if (currentPeriodRange.isStartEqualToEnd()) {
            return 1;
        }
        return (int) Stream.iterate(currentPeriodRange.startPeriod(), date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(currentPeriodRange.startPeriod(), currentPeriodRange.endPeriod()) + 1)
                .filter(this::isWorkingDay)
                .count();
    }

    private boolean isWorkingDay(LocalDate date) {
        return date.getDayOfWeek().getValue() < 6;
    }

    protected Double calculateGoalCompletionPercentage(int addedWords, int newWordsGoalForPeriod) {
        return ((double) addedWords / newWordsGoalForPeriod) * 100;
    }

    protected Double calculateGoalCompletionPercentage(
            int addedWord, int previousAddedWord, int newWordsGoalForPeriod) {
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(addedWord, newWordsGoalForPeriod);
        Double previousGoalCompletionPercentage =
                calculateGoalCompletionPercentage(previousAddedWord, newWordsGoalForPeriod);
        return goalCompletionPercentage + previousGoalCompletionPercentage;
    }

    protected double roundToThreeDigitAfterComma(Double value) {
        String formatted = DECIMAL_FORMAT.format(value);
        return Double.parseDouble(formatted);
    }

    protected DictionaryWordAdditionGoalReport buildReportByDictionary(
            Long dictionaryId, Double goalCompletionPercentage, int newWordsGoal) {
        return new DictionaryWordAdditionGoalReport(dictionaryId, goalCompletionPercentage, newWordsGoal, 1);
    }

    private record CalculationResultData(PeriodRange currentPeriodRange, Double goalCompletionPercentage,
                                         int newWordsGoalForPeriod) {
    }
}
