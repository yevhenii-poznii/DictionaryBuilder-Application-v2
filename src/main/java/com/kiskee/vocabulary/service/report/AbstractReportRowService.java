package com.kiskee.vocabulary.service.report;

import com.kiskee.vocabulary.model.dto.report.PeriodRange;
import com.kiskee.vocabulary.model.dto.report.goal.ReportData;
import com.kiskee.vocabulary.model.entity.report.DictionaryReport;
import com.kiskee.vocabulary.model.entity.report.ReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractReportRowService<
        D extends ReportData<V>, RR extends ReportRow<DR>, DR extends DictionaryReport<V>, V> {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    public abstract String getRowPeriod();

    protected abstract RR buildPeriodRow(
            PeriodRange currentPeriodRange, int workingDaysForPeriod, Set<DR> dictionaryReports);

    protected abstract V calculateGoalForPeriod(V dailyGoal, int workingDaysForPeriod);

    protected abstract V calculateGoalForPeriod(
            int currentWorkingDays, int previousWorkingDays, V previousGoal, V goalForToday);

    protected abstract Double calculateGoalCompletionPercentage(V value, V goalForPeriod);

    protected abstract DR buildReportByDictionary(
            Long dictionaryId, Double goalCompletionPercentage, V goalForPeriod, V value);

    protected abstract DR buildReportByDictionary(Long dictionaryId);

    protected abstract RR rebuildRow(
            RR row, PeriodRange currentPeriodRange, int workingDaysForPeriod, Set<DR> recalculatedDictionaryReports);

    public RR buildRowFromScratch(D reportData) {
        PeriodRange currentPeriodRange = buildPeriodRange(reportData.getCurrentDate(), reportData.getUserCreatedAt());
        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);
        V goalForPeriod = calculateGoalForPeriod(reportData.getDailyGoal(), workingDaysForPeriod);
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(reportData.getValue(), goalForPeriod);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);

        DR reportByDictionary = buildReportByDictionary(
                reportData.getDictionaryId(), goalCompletionPercentage, goalForPeriod, reportData.getValue());

        return buildPeriodRow(currentPeriodRange, workingDaysForPeriod, Set.of(reportByDictionary));
    }

    public RR updateRow(RR row, D reportData) {
        String rowPeriod = getRowPeriod();
        LocalDate lastDayOfPeriod = ReportPeriodUtil.getLastDayOfPeriod(row.getEndPeriod(), rowPeriod);
        if (reportData.getCurrentDate().isAfter(lastDayOfPeriod) && !rowPeriod.equals(ReportPeriodUtil.TOTAL)) {
            return buildRowFromScratch(reportData);
        }
        PeriodRange currentPeriodRange = buildPeriodRange(reportData.getCurrentDate(), reportData.getUserCreatedAt());
        int workingDaysForPeriod = calculateWorkingDaysForPeriod(currentPeriodRange);

        Set<DR> recalculatedDictionaryReports = updateDictionaryReports(row, reportData, workingDaysForPeriod);

        return rebuildRow(row, currentPeriodRange, workingDaysForPeriod, recalculatedDictionaryReports);
    }

    private PeriodRange buildPeriodRange(LocalDate getCurrentDate, LocalDate userCreatedAt) {
        String rowPeriod = getRowPeriod();
        PeriodRange currentPeriodRange = ReportPeriodUtil.getCurrentPeriodRange(getCurrentDate, rowPeriod);

        return userWasCreatedBeforeStartPeriod(currentPeriodRange.startPeriod(), userCreatedAt)
                ? currentPeriodRange
                : new PeriodRange(userCreatedAt, getCurrentDate);
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

    private Set<DR> updateDictionaryReports(RR row, D reportData, int workingDaysForPeriod) {
        Set<DR> dictionaryWordAdditionGoalReports = new HashSet<>(row.getDictionaryReports());

        Optional<DR> existingDictionaryReport = dictionaryWordAdditionGoalReports.stream()
                .filter(dictionaryReport -> dictionaryReport.getDictionaryId().equals(reportData.getDictionaryId()))
                .findFirst();

        if (existingDictionaryReport.isEmpty()) {
            DR newDictionaryReport = buildReportByDictionary(reportData.getDictionaryId());
            dictionaryWordAdditionGoalReports.add(newDictionaryReport);
        }

        return dictionaryWordAdditionGoalReports.stream()
                .map(dictionaryReport ->
                        recalculateDictionaryReport(row, workingDaysForPeriod, dictionaryReport, reportData))
                .collect(Collectors.toSet());
    }

    private DR recalculateDictionaryReport(RR row, int currentWorkingDays, DR dictionaryReport, D reportData) {
        Double goalCompletionPercentage = recalculateGoalCompletionPercentageForDictionaryReport(
                row, dictionaryReport, reportData, currentWorkingDays);
        goalCompletionPercentage = roundToThreeDigitAfterComma(goalCompletionPercentage);
        V goalForPeriod = calculateGoalForPeriod(
                currentWorkingDays,
                row.getWorkingDays(),
                dictionaryReport.getGoalForPeriod(),
                reportData.getDailyGoal());

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
                reportData.getDailyGoal(),
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

    private double roundToThreeDigitAfterComma(Double value) {
        String formatted = DECIMAL_FORMAT.format(value);
        return Double.parseDouble(formatted);
    }
}
