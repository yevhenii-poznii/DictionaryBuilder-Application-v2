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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractWordAdditionGoalReportRowService {

    public abstract String getRowPeriod();

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    protected abstract WordAdditionGoalReportRow buildPeriodRow(
            PeriodRange currentPeriodRange,
            int workingDaysForPeriod,
            Set<DictionaryWordAdditionGoalReport> dictionaryReports);

    public WordAdditionGoalReportRow buildRowFromScratch(WordAdditionData wordAdditionData) {
        PeriodRange currentPeriodRange =
                buildPeriodRange(wordAdditionData.currentDate(), wordAdditionData.userCreatedAt());
        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);
        int newWordsGoalForPeriod = wordAdditionData.newWordsPerDayGoal() * workingDaysForPeriod;
        Double goalCompletionPercentage =
                calculateGoalCompletionPercentage(wordAdditionData.addedWords(), newWordsGoalForPeriod);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);

        DictionaryWordAdditionGoalReport reportByDictionary = buildReportByDictionary(
                wordAdditionData.dictionaryId(),
                goalCompletionPercentage,
                newWordsGoalForPeriod,
                wordAdditionData.addedWords());

        return buildPeriodRow(currentPeriodRange, workingDaysForPeriod, Set.of(reportByDictionary));
    }

    public WordAdditionGoalReportRow updateRow(WordAdditionGoalReportRow row, WordAdditionData wordAdditionData) {
        String rowPeriod = getRowPeriod();
        LocalDate lastDayOfPeriod = ReportPeriodUtil.getLastDayOfPeriod(row.getEndPeriod(), rowPeriod);
        if (wordAdditionData.currentDate().isAfter(lastDayOfPeriod) && !rowPeriod.equals(ReportPeriodUtil.TOTAL)) {
            return buildRowFromScratch(wordAdditionData);
        }
        PeriodRange currentPeriodRange =
                buildPeriodRange(wordAdditionData.currentDate(), wordAdditionData.userCreatedAt());
        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);

        Set<DictionaryWordAdditionGoalReport> recalculatedDictionaryReports = row.getDictionaryReports().stream()
                .map(dictionaryReport ->
                        updateDictionaryReport(row, workingDaysForPeriod, dictionaryReport, wordAdditionData))
                .collect(Collectors.toSet());

        return row.toBuilder()
                .reportPeriod(row.getRowPeriod())
                .startPeriod(currentPeriodRange.startPeriod())
                .endPeriod(currentPeriodRange.endPeriod())
                .workingDays(workingDaysForPeriod)
                .dictionaryReports(recalculatedDictionaryReports)
                .build();
    }

    private PeriodRange buildPeriodRange(LocalDate currentDate, LocalDate userCreatedAt) {
        String rowPeriod = getRowPeriod();
        PeriodRange currentPeriodRange = ReportPeriodUtil.getCurrentPeriodRange(currentDate, rowPeriod);

        return userWasCreatedBeforeStartPeriod(currentPeriodRange.startPeriod(), userCreatedAt)
                ? currentPeriodRange
                : new PeriodRange(userCreatedAt, currentDate);
    }

    private boolean userWasCreatedBeforeStartPeriod(LocalDate startPeriod, LocalDate userCreatedAt) {
        return userCreatedAt.isBefore(startPeriod) && !getRowPeriod().equals(ReportPeriodUtil.TOTAL);
    }

    private int calculateWorkingDaysForPeriod(PeriodRange currentPeriodRange) {
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

    private DictionaryWordAdditionGoalReport updateDictionaryReport(
            WordAdditionGoalReportRow row,
            int currentWorkingDays,
            DictionaryWordAdditionGoalReport dictionaryReport,
            WordAdditionData wordAdditionData) {
        Double goalCompletionPercentage = recalculateGoalCompletionPercentageForDictionaryReport(
                row, dictionaryReport, wordAdditionData, currentWorkingDays);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);
        int newWordsGoalForPeriod = calculateNewWordsGoalForPeriod(
                currentWorkingDays,
                row.getWorkingDays(),
                dictionaryReport.getNewWordsGoal(),
                wordAdditionData.newWordsPerDayGoal());

        return dictionaryReport.buildFrom(
                goalCompletionPercentage, newWordsGoalForPeriod, wordAdditionData.addedWords());
    }

    private Double recalculateGoalCompletionPercentageForDictionaryReport(
            WordAdditionGoalReportRow row,
            DictionaryWordAdditionGoalReport dictionaryReport,
            WordAdditionData wordAdditionData,
            int currentWorkingDays) {
        if (!dictionaryReport.getDictionaryId().equals(wordAdditionData.dictionaryId())) {
            return calculateGoalCompletionPercentageWithoutNewWords(
                    dictionaryReport.getGoalCompletionPercentage(), row.getWorkingDays(), currentWorkingDays);
        }
        return calculateGoalCompletionPercentage(
                wordAdditionData.addedWords(),
                wordAdditionData.newWordsPerDayGoal(),
                dictionaryReport.getGoalCompletionPercentage(),
                row.getWorkingDays(),
                currentWorkingDays);
    }

    private Double calculateGoalCompletionPercentage(int addedWords, int newWordsGoalForPeriod) {
        return ((double) addedWords / newWordsGoalForPeriod) * 100;
    }

    private Double calculateGoalCompletionPercentage(
            int addedWord,
            int newWordsGoalForToday,
            Double previousGoalCompletionPercentage,
            int previousWorkingDays,
            int currentWorkingDays) {
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(addedWord, newWordsGoalForToday);
        return ((previousGoalCompletionPercentage * previousWorkingDays) + goalCompletionPercentage)
                / currentWorkingDays;
    }

    private Double calculateGoalCompletionPercentageWithoutNewWords(
            Double previousGoalCompletionPercentage, int previousWorkingDays, int currentWorkingDays) {
        return (previousGoalCompletionPercentage * previousWorkingDays) / currentWorkingDays;
    }

    private double roundToThreeDigitAfterComma(Double value) {
        String formatted = DECIMAL_FORMAT.format(value);
        return Double.parseDouble(formatted);
    }

    private DictionaryWordAdditionGoalReport buildReportByDictionary(
            Long dictionaryId, Double goalCompletionPercentage, int newWordsGoal, int addedWords) {
        return new DictionaryWordAdditionGoalReport(dictionaryId, goalCompletionPercentage, newWordsGoal, addedWords);
    }

    private int calculateNewWordsGoalForPeriod(
            int currentWorkingDays, int previousWorkingDays, int previousWordsGoal, int newWordsGoalForToday) {
        int currentWorkingDaysWithoutCurrentDay = currentWorkingDays - 1; // excluding current day
        double previousDailyAverageWordsGoal = (double) previousWordsGoal / previousWorkingDays;
        double previousWordsGoalForPeriod = previousDailyAverageWordsGoal * currentWorkingDaysWithoutCurrentDay;
        return (int) (previousWordsGoalForPeriod + newWordsGoalForToday);
    }
}
