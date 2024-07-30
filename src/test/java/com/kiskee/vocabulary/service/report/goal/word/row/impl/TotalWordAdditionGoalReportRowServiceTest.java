package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.update.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.TotalWordAdditionGoalReportRow;
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
public class TotalWordAdditionGoalReportRowServiceTest {

    @InjectMocks
    private TotalWordAdditionGoalReportRowService totalWordAdditionGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testBuildRowFromScratch_WhenTotalRowDoesNotExist_ThenBuildRowFromScratchWithDateFromUserCreatedAt(
            TestData testData) {
        WordAdditionGoalReportRow totalRow = totalWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        WordAdditionGoalReportRow totalRow = totalWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 17.5, 40, 7);
        TotalWordAdditionGoalReportRow totalRowForToday = TotalWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow totalRow =
                totalWordAdditionGoalReportRowService.updateRow(totalRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 17.5, 40, 7);
        TotalWordAdditionGoalReportRow totalRowForToday = TotalWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow totalRow =
                totalWordAdditionGoalReportRowService.updateRow(totalRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 17.5, 40, 7);
        TotalWordAdditionGoalReportRow totalRowForToday = TotalWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow totalRow =
                totalWordAdditionGoalReportRowService.updateRow(totalRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 17.5, 40, 7);
        TotalWordAdditionGoalReportRow totalRowForToday = TotalWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(4)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow totalRow =
                totalWordAdditionGoalReportRowService.updateRow(totalRowForToday, testData.data());

        DictionaryWordAdditionGoalReport recalculatedExistingDictionaryReport =
                dictionaryGoalReport.buildFrom(14.0, dictionaryGoalReport.getNewWordsGoal() + 10);

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
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 9);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> buildRowFromScratchTestDataWithSkippedDays() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                Double.parseDouble(DECIMAL_FORMAT.format((double) i / (newWordsGoal * 4) * 100)),
                                newWordsGoal * 4,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndExistingDictionaryReport() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format(((double) (i + 7) / (newWordsGoal * 4)) * 100)),
                                newWordsGoal * 4,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndNewDictionaryReport() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 14);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format((double) i / (newWordsGoal * 4) * 100)),
                                newWordsGoal * 4,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDay() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 15);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format(((double) (i + 7) / (newWordsGoal * 5)) * 100)),
                                newWordsGoal * 5,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 5, 9);
        LocalDate currentDate = LocalDate.of(2024, 5, 15);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                Double.parseDouble(decimalFormat.format((double) i / (newWordsGoal * 5) * 100)),
                                newWordsGoal * 5,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private record TestData(WordAdditionData data, DictionaryWordAdditionGoalReport expectedDictionaryGoalReport) {}
}
