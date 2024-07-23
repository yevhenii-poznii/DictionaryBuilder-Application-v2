package com.kiskee.vocabulary.service.report.goal.word.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.DictionaryWordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
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
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().currentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().currentDate());
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
        assertThat(monthlyRow.getStartPeriod()).isEqualTo(testData.data().currentDate());
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().currentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestDataWhenUserCreatedAfterStartPeriodAndWasSkippedFewDays")
    void
            testBuildRowFromScratch_WhenMonthlyRowDoesNotExistAndUserWasCreatedAfterReportStartPeriodAndWasSkippedFewDays_ThenBuildRowFromScratchWithUserCreatedDateAsStartPeriod(
                    TestData testData) {
        WordAdditionGoalReportRow monthlyRow =
                monthlyWordAdditionGoalReportRowService.buildRowFromScratch(testData.data());

        System.out.println(monthlyRow);
        assertThat(monthlyRow.getWorkingDays()).isEqualTo(4);
        assertThat(monthlyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.MONTH);
        assertThat(monthlyRow.getStartPeriod())
                .isEqualTo(testData.data().currentDate().minusDays(3));
        assertThat(monthlyRow.getEndPeriod()).isEqualTo(testData.data().currentDate());
        assertThat(monthlyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
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

    private record TestData(WordAdditionData data, DictionaryWordAdditionGoalReport expectedDictionaryGoalReport) {}
}
