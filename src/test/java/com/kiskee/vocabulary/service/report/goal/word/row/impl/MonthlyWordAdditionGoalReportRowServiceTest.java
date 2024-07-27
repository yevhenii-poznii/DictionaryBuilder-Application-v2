package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.word.period.MonthlyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.text.DecimalFormat;
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
public class MonthlyWordAdditionGoalReportRowServiceTest {

    @InjectMocks
    private MonthlyWordAdditionGoalReportRowService monthlyWordAdditionGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testBuildRowFromScratch_WhenMonthlyRowDoesNotExistAndUserWasCreatedBeforeReportStartPeriod_ThenBuildRowFromScratchWithStartPeriod(
                    TestData testData) {
        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 70.0, 10, 7);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 70.0, 10, 7);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 70.0, 10, 7);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 70.0, 10, 7);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        DictionaryWordAdditionGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                dictionaryGoalReport.getGoalCompletionPercentage() / 2, dictionaryGoalReport.getNewWordsGoal() * 2);

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 70.0, 50, 35);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 70.0, 50, 35);
        MonthlyWordAdditionGoalReportRow monthlyRowForToday = MonthlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(7))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.updateRow(monthlyRowForToday, testData.data());

        assertThat(monthlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(monthlyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 1);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 16);
        LocalDate currentDate = LocalDate.of(2024, 7, 16);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriodAndWasSkippedFewDays() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 8);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                Double.parseDouble(DECIMAL_FORMAT.format(((double) i / (newWordsGoal * 4)) * 100)),
                                newWordsGoal * 4,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndExistingDictionaryReport() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 1);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format(((double) (i + 7) / newWordsGoal) * 100)),
                                newWordsGoal,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriod() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 2);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format(((double) (i + 7) / (newWordsGoal * 2)) * 100)),
                                newWordsGoal * 2,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriodAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 2);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format((double) i / (newWordsGoal * 2) * 100)),
                                newWordsGoal * 2,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNotWorkingDay() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 7);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format((double) (i + 35) / (newWordsGoal * 5) * 100)),
                                newWordsGoal * 5,
                                i + 35)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowWhenCurrentDayIsAfterLastDayOfPreviousPeriod() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 6, 9);
        LocalDate currentDate = LocalDate.of(2024, 8, 1);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private record TestData(WordAdditionData data, DictionaryWordAdditionGoalReport expectedDictionaryGoalReport) {}
}
