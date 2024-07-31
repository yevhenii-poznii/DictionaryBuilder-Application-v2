package com.kiskee.dictionarybuilder.service.report.goal.time.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.dictionarybuilder.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.YearlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
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
public class YearlyRepetitionTimeSpendGoalReportRowServiceTest {

    @InjectMocks
    private YearlyRepetitionTimeSpendGoalReportRowService yearlyRepetitionTimeSpendGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");
    private static final String DICTIONARY_NAME = "SomeDictionaryName";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    private static final Duration repetitionDuration = Duration.ofMinutes(1);
    private static final Duration repetitionDurationGoal = Duration.ofHours(1);

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testBuildRowFromScratch_WhenYearlyRowDoesNotExistAndUserWasCreatedBeforeReportStartPeriod_ThenBuildRowFromScratchWithStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod")
    void
            testBuildRowFromScratch_WhenYearlyRowDoesNotExistAndUserWasCreatedAfterReportStartPeriod_ThenBuildRowFromScratchWithUserCreatedDateAsStartPeriod(
                    TestData testData) {
        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(14);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(17));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndExistingDictionaryReport")
    void testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportExists_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 8.333, Duration.ofHours(14), Duration.ofMinutes(70), 7);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(17))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(14)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(14);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(17));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndNewDictionaryReport")
    void
            testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportDoesNotExist_ThenRecalculateRowWithNewDictionaryReport(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 8.333, Duration.ofHours(14), Duration.ofMinutes(70), 7);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(17))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(14)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(14);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(17));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(dictionaryGoalReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriod")
    void testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriod_ThenRecalculateRowForNewDay(
            TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 33.333, Duration.ofHours(1), Duration.ofMinutes(20), 2);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(2);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayInTheSamePeriodAndForNewDictionaryId")
    void
            testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriodButGivenNewDictionaryId_ThenRecalculateRowForNewDay(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 33.333, Duration.ofHours(1), Duration.ofMinutes(20), 2);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        DictionaryRepetitionTimeSpendGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                Double.parseDouble(DECIMAL_FORMAT.format(dictionaryGoalReport.getGoalCompletionPercentage() / 2)),
                dictionaryGoalReport.getRepetitionTimeGoal().multipliedBy(2));

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(2);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(1));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(
                        recalculatedExistingDictionaryReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNotWorkingDay")
    void testUpdateRow_WhenCurrentDayIsNotWorkingDay_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 23.333, Duration.ofHours(5), Duration.ofMinutes(70), 7);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(5);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(6));
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod")
    void testUpdateRow_WhenCurrentDayIsAfterLastDayOfPreviousPeriod_ThenBuildRowFromScratch(TestData testData) {
        YearlyRepetitionTimeSpendGoalReportRow yearlyRowForToday = YearlyRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(7))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .build();

        RepetitionTimeSpendGoalReportRow yearlyRow =
                yearlyRepetitionTimeSpendGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 1);
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
        LocalDate userCreatedAt = LocalDate.of(2024, 4, 16);
        LocalDate currentDate = LocalDate.of(2024, 5, 3);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(14).toSeconds())
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
                                    repetitionDurationGoal.multipliedBy(14),
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndExistingDictionaryReport() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 18);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((8.333 * 14) + goalCompletionPercentage) / 14;
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
                                    repetitionDurationGoal.multipliedBy(14),
                                    repetitionDuration.multipliedBy(i + 70),
                                    8));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndNewDictionaryReport() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 18);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(14).toSeconds())
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
                                    repetitionDurationGoal.multipliedBy(14),
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriod() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((33.333 * 1) + goalCompletionPercentage) / 2;
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
                                    repetitionDuration.multipliedBy(i + 20),
                                    3));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriodAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 2);
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
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 7);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((23.333 * 5) + goalCompletionPercentage) / 5;
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
                                    repetitionDuration.multipliedBy(i + 70),
                                    8));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2025, 1, 1);
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
