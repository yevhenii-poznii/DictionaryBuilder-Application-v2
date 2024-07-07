package com.kiskee.vocabulary.service.report.goal.word.row;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public abstract class AbstractWordAdditionGoalReportRowService {

    protected abstract WordAdditionGoalReportRow buildPeriodRow();

    public WordAdditionGoalReportRow buildRowFromScratch(WordAdditionData wordAdditionData) {
        ZoneId userTimeZone = TimeZoneContextHolder.getTimeZone();
        LocalDate currentDateAtUserTimeZone = LocalDate.now(userTimeZone);
        LocalDate userCreatedAt = wordAdditionData.userCreatedAt().atZone(userTimeZone).toLocalDate();

        boolean before = userCreatedAt.isBefore(currentDateAtUserTimeZone);

        int workingDays = null;

        Double goalCompletionPercentage = calculateGoalCompletionPercentage(wordAdditionData.newWordsPerDayGoal());

        DictionaryWordAdditionGoalReport reportByDictionary = buildReportByDictionary(
                goalCompletionPercentage, wordAdditionData.newWordsPerDayGoal());

        return buildPeriodRow();
    }

    protected int calculateWorkingDays(LocalDate previousDay, LocalDate currentDay) {
        if (previousDay.isEqual(currentDay)) {
            return 1;
        }

        return (int) Stream.iterate(previousDay.plusDays(1), date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(previousDay, currentDay))
                .filter(date -> date.getDayOfWeek().getValue() < 6)
                .count();
    }

    private void shos() {

    }

    protected Double calculateGoalCompletionPercentage(int newWordsPerDayGoal) {
        return ((double) 1 / newWordsPerDayGoal) * 100;
    }

    protected Double calculateGoalCompletionPercentage(int newWordsPerDayGoal, Double previousGoalCompletionPercentage) {
        Double goalCompletionPercentage = calculateGoalCompletionPercentage(newWordsPerDayGoal);
        return goalCompletionPercentage + previousGoalCompletionPercentage;
    }

    protected DictionaryWordAdditionGoalReport buildReportByDictionary(
            Double goalCompletionPercentage, int newWordsGoal) {
        return new DictionaryWordAdditionGoalReport(goalCompletionPercentage, newWordsGoal, 1);
    }
}
