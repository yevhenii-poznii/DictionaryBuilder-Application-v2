package com.kiskee.vocabulary.service.report.goal.time.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.DailyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DailyRepetitionTimeSpendGoalReportRowServiceTest {

    @InjectMocks
    private DailyRepetitionTimeSpendGoalReportRowService dailyRepetitionTimeSpendGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testBuildRowFromScratch_WhenDailyRowDoesNotExist_ThenBuildRowFromScratch(TestData testData) {
        RepetitionTimeSpendGoalReportRow dailyRow =
                dailyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(dailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(dailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(dailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowTestData")
    void testUpdateRow_WhenDailyRowExistsForToday_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        DailyRepetitionTimeSpendGoalReportRow dailyRowForToday = DailyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow updatedDailyRow =
                dailyRepetitionTimeSpendGoalReportRowService.updateRow(dailyRowForToday, testData.data());

        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testUpdateRow_WhenDailyRowExistsForTodayAndGivenNewDictionary_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        DailyRepetitionTimeSpendGoalReportRow dailyRowForToday = DailyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow updatedDailyRow =
                dailyRepetitionTimeSpendGoalReportRowService.updateRow(dailyRowForToday, testData.data());

        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(dictionaryGoalReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testUpdateRow_WhenDailyRowExistsAndCurrentDateIsAfterPreviousDailyRow_ThenBuildRowFromScratch(
            TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        DailyRepetitionTimeSpendGoalReportRow dailyRowForToday = DailyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow updatedDailyRow =
                dailyRepetitionTimeSpendGoalReportRowService.updateRow(dailyRowForToday, testData.data());

        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = Double.parseDouble(DECIMAL_FORMAT.format(goalCompletionPercentage));
                    return new TestData(
                            new RepetitionTimeSpendData(
                                    USER_ID,
                                    dictionaryId,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    goalCompletionPercentage,
                                    repetitionDurationGoal,
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowTestData() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = goalCompletionPercentage + 16.667;
                    goalCompletionPercentage = Double.parseDouble(DECIMAL_FORMAT.format(goalCompletionPercentage));
                    return new TestData(
                            new RepetitionTimeSpendData(
                                    USER_ID,
                                    dictionaryId,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    1L,
                                    dictionaryId,
                                    goalCompletionPercentage,
                                    repetitionDurationGoal,
                                    repetitionDuration.multipliedBy(i + 10),
                                    2));
                })
                .toList();

        return testDataList.stream();
    }

    private record TestData(
            RepetitionTimeSpendData data, DictionaryRepetitionTimeSpendGoalReport expectedDictionaryGoalReport) {}
}
