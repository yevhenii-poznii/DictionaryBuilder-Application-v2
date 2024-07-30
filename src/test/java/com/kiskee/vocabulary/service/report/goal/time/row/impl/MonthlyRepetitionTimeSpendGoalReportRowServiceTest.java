package com.kiskee.vocabulary.service.report.goal.time.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.vocabulary.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.MonthlyRepetitionTimeSpendGoalReportRow;
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
public class MonthlyRepetitionTimeSpendGoalReportRowServiceTest {

    @InjectMocks
    private MonthlyRepetitionTimeSpendGoalReportRowService monthlyRepetitionTimeSpendGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");
    private static final String DICTIONARY_NAME = "SomeDictionaryName";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testBuildRowFromScratch_WhenMonthlyRowDoesNotExistAndUserWasCreatedBeforeReportStartPeriod_ThenBuildRowFromScratchWithStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod")
    void
            testBuildRowFromScratch_WhenMonthlyRowDoesNotExistAndUserWasCreatedAfterReportStartPeriod_ThenBuildRowFromScratchWithUserCreatedDateAsStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriodAndWasSkippedFewDays")
    void
            testBuildRowFromScratch_WhenMonthlyRowDoesNotExistAndUserWasCreatedAfterReportStartPeriodAndWasSkippedFewDays_ThenBuildRowFromScratchWithUserCreatedDateAsStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(4);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(3));
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndExistingDictionaryReport")
    void testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportExists_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportDoesNotExist_ThenRecalculateRowWithNewDictionaryReport(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(dictionaryGoalReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriod")
    void testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriod_ThenRecalculateRowForNewDay(
            TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(2);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriodAndForNewDictionaryId")
    void
            testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriodButGivenNewDictionaryId_ThenRecalculateRowForNewDay(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 16.667, Duration.ofHours(1), Duration.ofMinutes(10), 1);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        DictionaryRepetitionTimeSpendGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                Double.parseDouble(DECIMAL_FORMAT.format(dictionaryGoalReport.getGoalCompletionPercentage() / 2)),
                dictionaryGoalReport.getRepetitionTimeGoal().multipliedBy(2));

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(2);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(
                        recalculatedExistingDictionaryReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNotWorkingDay")
    void testUpdateRow_WhenCurrentDayIsNotWorkingDay_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 66.667, Duration.ofHours(5), Duration.ofMinutes(200), 20);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(5);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(6));
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod")
    void testUpdateRow_WhenCurrentDayIsAfterLastDayOfPreviousPeriod_ThenBuildRowFromScratch(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 66.667, Duration.ofHours(5), Duration.ofMinutes(200), 20);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRowForToday = MonthlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(7))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow monthlyRow =
                monthlyRepetitionTimeSpendGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 1);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    DICTIONARY_NAME,
                                    goalCompletionPercentage,
                                    repetitionDurationGoal,
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriodAndWasSkippedFewDays() {
        Long dictionaryId = 10L;
        Duration repetitionDuration = Duration.ofMinutes(1);
        Duration repetitionDurationGoal = Duration.ofHours(1);
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 8);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(4).toSeconds())
                            * 100;
                    goalCompletionPercentage = Double.parseDouble(DECIMAL_FORMAT.format(goalCompletionPercentage));
                    return new TestData(
                            new RepetitionTimeSpendData(
                                    USER_ID,
                                    dictionaryId,
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    DICTIONARY_NAME,
                                    goalCompletionPercentage,
                                    repetitionDurationGoal.multipliedBy(4),
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
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 1);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    1L,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 2);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    1L,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 2);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 7);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    1L,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 8, 1);
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
                                    DICTIONARY_NAME,
                                    repetitionDuration.multipliedBy(i),
                                    repetitionDurationGoal,
                                    userCreatedAt,
                                    currentDate),
                            new DictionaryRepetitionTimeSpendGoalReport(
                                    null,
                                    dictionaryId,
                                    DICTIONARY_NAME,
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
