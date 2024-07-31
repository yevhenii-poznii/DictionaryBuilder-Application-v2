package com.kiskee.dictionarybuilder.service.report.goal.time.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.dictionarybuilder.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.DictionaryRepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.TotalRepetitionTimeSpendGoalReportRow;
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
public class TotalRepetitionTimeSpendGoalReportRowServiceTest {

    @InjectMocks
    private TotalRepetitionTimeSpendGoalReportRowService totalRepetitionTimeSpendGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");
    private static final String DICTIONARY_NAME = "SomeDictionaryName";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    private static final Duration repetitionDuration = Duration.ofMinutes(1);
    private static final Duration repetitionDurationGoal = Duration.ofHours(1);

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testBuildRowFromScratch_WhenTotalRowDoesNotExist_ThenBuildRowFromScratchWithDateFromUserCreatedAt(
            TestData testData) {
        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(totalRow.getWorkingDays()).isEqualTo(1);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWithSkippedDays")
    void testBuildRowFromScratch_WhenTotalRowDoesNotExistAndSkippedFewDaysFromUserCreatedDate_ThenBuildRowFromScratch(
            TestData testData) {
        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(totalRow.getWorkingDays()).isEqualTo(4);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(5));
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndExistingDictionaryReport")
    void testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportExists_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 29.166, Duration.ofHours(4), Duration.ofMinutes(70), 7);
        TotalRepetitionTimeSpendGoalReportRow totalRowForToday = TotalRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.updateRow(totalRowForToday, testData.data());

        assertThat(totalRow.getWorkingDays()).isEqualTo(4);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(5));
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForTodayAndNewDictionaryReport")
    void
            testUpdateRow_WhenRowExistsForTodayAndGivenDictionaryReportDoesNotExist_ThenRecalculateRowWithNewDictionaryReport(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 29.166, Duration.ofHours(4), Duration.ofMinutes(70), 7);
        TotalRepetitionTimeSpendGoalReportRow totalRowForToday = TotalRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.updateRow(totalRowForToday, testData.data());

        assertThat(totalRow.getWorkingDays()).isEqualTo(4);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(5));
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports())
                .containsExactlyInAnyOrder(dictionaryGoalReport, testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDay")
    void testUpdateRow_WhenRowExistsForPreviousDay_ThenRecalculateRowForNewDay(TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 10L, DICTIONARY_NAME, 29.166, Duration.ofHours(4), Duration.ofMinutes(70), 7);
        TotalRepetitionTimeSpendGoalReportRow totalRowForToday = TotalRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.updateRow(totalRowForToday, testData.data());

        assertThat(totalRow.getWorkingDays()).isEqualTo(5);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(6));
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowForNextDayAndForNewDictionaryId")
    void
            testUpdateRow_WhenRowExistsForPreviousDayAndCurrentDateInTheSamePeriodButGivenNewDictionaryId_ThenRecalculateRowForNewDay(
                    TestData testData) {
        DictionaryRepetitionTimeSpendGoalReport dictionaryGoalReport = new DictionaryRepetitionTimeSpendGoalReport(
                1L, 5L, DICTIONARY_NAME, 29.166, Duration.ofHours(4), Duration.ofMinutes(70), 7);
        TotalRepetitionTimeSpendGoalReportRow totalRowForToday = TotalRepetitionTimeSpendGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionTimeSpendGoalReportRow totalRow =
                totalRepetitionTimeSpendGoalReportRowService.updateRow(totalRowForToday, testData.data());

        DictionaryRepetitionTimeSpendGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                23.333, dictionaryGoalReport.getRepetitionTimeGoal().plus(Duration.ofHours(1)));

        assertThat(totalRow.getWorkingDays()).isEqualTo(5);
        assertThat(totalRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.TOTAL);
        assertThat(totalRow.getStartPeriod())
                .isEqualTo(testData.data().getCurrentDate().minusDays(6));
        assertThat(totalRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(totalRow.getDictionaryReports())
                .containsExactlyInAnyOrder(
                        recalculatedExistingDictionaryReport, testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 9);
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

    private static Stream<TestData> buildRowFromScratchTestDataWithSkippedDays() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
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
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((29.166 * 4) + goalCompletionPercentage) / 4;
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
                                    repetitionDurationGoal.multipliedBy(4),
                                    repetitionDuration.multipliedBy(i + 70),
                                    8));
                })
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndNewDictionaryReport() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
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

    private static Stream<TestData> updateRowForNextDay() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 15);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage =
                            ((double) repetitionDuration.multipliedBy(i).toSeconds()
                                            / repetitionDurationGoal.toSeconds())
                                    * 100;
                    goalCompletionPercentage = ((29.166 * 4) + goalCompletionPercentage) / 5;
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

    private static Stream<TestData> updateRowForNextDayAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 15);
        List<TestData> testDataList = IntStream.range(1, 12)
                .map(i -> i * 10)
                .mapToObj(i -> {
                    double goalCompletionPercentage = ((double)
                                            repetitionDuration.multipliedBy(i).toSeconds()
                                    / repetitionDurationGoal.multipliedBy(5).toSeconds())
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
                                    repetitionDurationGoal.multipliedBy(5),
                                    repetitionDuration.multipliedBy(i),
                                    1));
                })
                .toList();

        return testDataList.stream();
    }

    private record TestData(
            RepetitionTimeSpendData data, DictionaryRepetitionTimeSpendGoalReport expectedDictionaryGoalReport) {}
}
