package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.word.period.DailyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
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
public class DailyWordAdditionGoalReportRowServiceTest {

    @InjectMocks
    private DailyWordAdditionGoalReportRowService dailyWordAdditionGoalReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testBuildRowFromScratch_WhenDailyRowDoesNotExist_ThenBuildRowFromScratch(TestData testData) {
        WordAdditionGoalReportRow dailyRow = dailyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

        assertThat(dailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(dailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(dailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowTestData")
    void testUpdateRow_WhenDailyRowExistsForToday_ThenRecalculateRow(TestData testData) {
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 10L, 10.0, 10, 1);
        DailyWordAdditionGoalReportRow dailyRowForToday = DailyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow updatedDailyRow =
                dailyWordAdditionGoalReportRowService.updateRow(dailyRowForToday, testData.data());
        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testUpdateRow_WhenDailyRowExistsForTodayAndGivenNewDictionary_ThenRecalculateRow(TestData testData) {
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 10.0, 10, 1);
        DailyWordAdditionGoalReportRow dailyRowForToday = DailyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow updatedDailyRow =
                dailyWordAdditionGoalReportRowService.updateRow(dailyRowForToday, testData.data());
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
        DictionaryWordAdditionGoalReport dictionaryGoalReport =
                new DictionaryWordAdditionGoalReport(1L, 5L, 70.0, 10, 7);
        DailyWordAdditionGoalReportRow dailyRowForToday = DailyWordAdditionGoalReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        WordAdditionGoalReportRow updatedDailyRow =
                dailyWordAdditionGoalReportRowService.updateRow(dailyRowForToday, testData.data());
        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                null, dictionaryId, (double) i * newWordsGoal, newWordsGoal, i)))
                .toList();

        return testDataList.stream();
    }

    private static Stream<TestData> updateRowTestData() {
        Long dictionaryId = 10L;
        int newWordsGoal = 10;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        List<TestData> testDataList = IntStream.range(1, 12)
                .mapToObj(i -> new TestData(
                        new WordAdditionData(USER_ID, dictionaryId, i, newWordsGoal, userCreatedAt, currentDate),
                        new DictionaryWordAdditionGoalReport(
                                1L, dictionaryId, (double) (i + 1) * newWordsGoal, newWordsGoal, i + 1)))
                .toList();

        return testDataList.stream();
    }

    private record TestData(WordAdditionData data, DictionaryWordAdditionGoalReport expectedDictionaryGoalReport) {}
}
