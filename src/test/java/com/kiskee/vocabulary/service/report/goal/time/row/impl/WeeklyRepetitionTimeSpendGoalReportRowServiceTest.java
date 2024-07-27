package com.kiskee.vocabulary.service.report.goal.time.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.goal.RepetitionTimeSpendData;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.WeeklyRepetitionTimeSpendGoalReportRow;
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
public class WeeklyRepetitionTimeSpendGoalReportRowServiceTest {

    @InjectMocks
    private WeeklyRepetitionTimeSpendGoalReportRowService weeklyRepetitionTimeSpendGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testBuildRowFromScratch_WhenWeeklyRowDoesNotExistAndUserWasCreatedBeforeReportStartPeriod_ThenBuildRowFromScratchWithStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(1);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod")
    void
            testBuildRowFromScratch_WhenWeeklyRowDoesNotExistAndUserWasCreatedAfterReportStartPeriod_ThenBuildRowFromScratchWithUserCreatedDateAsStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(1);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenWasSkippedFewDaysFromStartPeriod")
    void
            testBuildRowFromScratch_WhenWeeklyRowDoesNotExistAndWasSkippedFewDaysFromStartPeriod_ThenBuildRowFromScratchWithSkippedDays(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(3);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(2));
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndExistingDictionaryReport")
    void testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportExists_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(1);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportDoesNotExist_ThenRecalculateRowWithNewDictionaryReport(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(1);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(dictionaryGoalReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriod")
    void testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriod_ThenRecalculateRowForNewDay(
            TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(2);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriodAndForNewDictionaryId")
    void
            testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriodButGivenNewDictionaryId_ThenRecalculateRowForNewDay(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        DictionaryRepetitionTimeSpendGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                Double.parseDouble(DECIMAL_FORMAT.format(dictionaryGoalReport.getGoalCompletionPercentage() / 2)),
                dictionaryGoalReport.getRepetitionTimeGoal().multipliedBy(2));

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(2);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(
                        recalculatedExistingDictionaryReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNotWorkingDay")
    void testUpdateRow_WhenCurrentDayIsNotWorkingDay_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, 66.667, Duration.ofHours(5), Duration.ofMinutes(200), 20);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        assertThat(weeklyRow.getWorkingDays()).isEqualTo(5);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(5));
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod")
    void testUpdateRow_WhenCurrentDayIsAfterLastDayOfPreviousPeriod_ThenBuildRowFromScratch(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, 66.667, Duration.ofHours(5), Duration.ofMinutes(200), 20);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRowForToday = WeeklyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(7))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow weeklyRow =
                weeklyRepetitionTimeSpendGoalReportRowService.updateRow(weeklyRowForToday, testData.data());

        System.out.println(weeklyRow);
        assertThat(weeklyRow.getWorkingDays()).isEqualTo(1);
        assertThat(weeklyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.WEEK);
        assertThat(weeklyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(weeklyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 15);
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

    private static Stream<TestData> buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 16);
        LocalDate currentDate = LocalDate.of(2024, 7, 16);
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

    private static Stream<TestData> buildRowFromScratchTestDataWhenWasSkippedFewDaysFromStartPeriod() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 17);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(3).toSeconds())
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
                                    repetitionDurationGoal.multipliedBy(3),
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndExistingDictionaryReport() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 15);
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

    private static Stream<TestData> updateRowForNextDayInTheSamePeriod() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 16);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((16.667 * 1) + goalCompletionPercentage) / 2;
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
                                    repetitionDurationGoal.multipliedBy(2),
                                    repetitionDuration.multipliedBy(i + 10),
                                    2));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriodAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 16);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(2).toSeconds())
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
                                    repetitionDurationGoal.multipliedBy(2),
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNotWorkingDay() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 20);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((66.667 * 5) + goalCompletionPercentage) / 5;
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
                                    repetitionDurationGoal.multipliedBy(5),
                                    repetitionDuration.multipliedBy(i + 200),
                                    21));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 22);
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

    private record TestData(
            RepetitionTimeSpendData data, DictionaryRepetitionTimeSpendGoalReport expectedDictionaryGoalReport) {}
}
