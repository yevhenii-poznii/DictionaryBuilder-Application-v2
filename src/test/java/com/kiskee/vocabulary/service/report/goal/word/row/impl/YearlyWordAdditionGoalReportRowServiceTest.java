package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.update.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.goal.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.YearlyWordAdditionGoalReportRow;
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
public class YearlyWordAdditionGoalReportRowServiceTest {

    @InjectMocks
    private YearlyWordAdditionGoalReportRowService yearlyWordAdditionGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");
    private static final String DICTIONARY_NAME = "SomeDictionaryName";

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void
            testBuildRowFromScratch_WhenYearlyRowDoesNotExistAndUserWasCreatedBeforeReportStartPeriod_ThenBuildRowFromScratchWithStartPeriod(
                    TestData testData) {
        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, DICTIONARY_NAME, 5.0, 140, 7);
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(17))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(14)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, DICTIONARY_NAME, 5.0, 140, 7);
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(17))
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(14)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, DICTIONARY_NAME, 70.0, 10, 7);
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, DICTIONARY_NAME, 70.0, 10, 7);
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        DictionaryWordAdditionGoalReport recalculatedExistingDictionaryReport = dictionaryGoalReport.buildFrom(
                dictionaryGoalReport.getGoalCompletionPercentage() / 2, dictionaryGoalReport.getNewWordsGoal() * 2);

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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, DICTIONARY_NAME, 70.0, 50, 35);
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(5))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

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
        YearlyWordAdditionGoalReportRow yearlyRowForToday = YearlyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(7))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(5)
                .build();

        WordAdditionGoalReportRow yearlyRow =
                yearlyWordAdditionGoalReportRowService.updateRow(yearlyRowForToday, testData.data());

        assertThat(yearlyRow.getWorkingDays()).isEqualTo(1);
        assertThat(yearlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.YEAR);
        assertThat(yearlyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(yearlyRow.getDictionaryReports()).containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 1);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, DICTIONARY_NAME, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriod() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 4, 16);
        LocalDate currentDate = LocalDate.of(2024, 5, 3);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                DICTIONARY_NAME,
                                Double.parseDouble(DECIMAL_FORMAT.format((double) i / (newWordsGoal * 14) * 100)),
                                newWordsGoal * 14,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndExistingDictionaryReport() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 18);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                DICTIONARY_NAME,
                                Double.parseDouble(
                                        decimalFormat.format(((double) (i + 7) / (newWordsGoal * 14)) * 100)),
                                newWordsGoal * 14,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForTodayAndNewDictionaryReport() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 18);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                DICTIONARY_NAME,
                                Double.parseDouble(decimalFormat.format((double) i / (newWordsGoal * 14) * 100)),
                                newWordsGoal * 14,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriod() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                DICTIONARY_NAME,
                                Double.parseDouble(decimalFormat.format(((double) (i + 7) / (newWordsGoal * 2)) * 100)),
                                newWordsGoal * 2,
                                i + 7)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNextDayInTheSamePeriodAndForNewDictionaryId() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 2);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null,
                                dictionaryId,
                                DICTIONARY_NAME,
                                Double.parseDouble(decimalFormat.format((double) i / (newWordsGoal * 2) * 100)),
                                newWordsGoal * 2,
                                i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowForNotWorkingDay() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2023, 12, 9);
        LocalDate currentDate = LocalDate.of(2024, 1, 7);
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L,
                                dictionaryId,
                                DICTIONARY_NAME,
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
        LocalDate currentDate = LocalDate.of(2025, 1, 1);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(
                                USER_ID, dictionaryId, DICTIONARY_NAME, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, DICTIONARY_NAME, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private record TestData(WordAdditionData data, DictionaryWordAdditionGoalReport expectedDictionaryGoalReport) {}
}
