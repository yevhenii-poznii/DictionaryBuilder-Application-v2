package com.kiskee.vocabulary.service.report.progress.repetition.row.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kiskee.vocabulary.model.dto.report.progress.repetition.RepetitionStatisticData;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.DictionaryRepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.DailyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DailyRepetitionStatisticReportRowServiceTest {

    @InjectMocks
    private DailyRepetitionStatisticReportRowService dailyRepetitionStatisticReportRowService;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testBuildRowFromScratch_WhenDailyRowDoesNotExist_ThenBuildRowFromScratch(TestData testData) {
        RepetitionStatisticReportRow dailyRow =
                dailyRepetitionStatisticReportRowService.buildRowFromScratch(testData.data());

        assertThat(dailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(dailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(dailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(dailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("updateRowTestData")
    void testUpdateRow_WhenDailyRowExistsForToday_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionStatisticReport dictionaryGoalReport =
                new DictionaryRepetitionStatisticReport(1L, 10L, 96.429, 56, 79.63, 43, 20.37, 11, 3.571, 2, 54, 1);
        DailyRepetitionStatisticReportRow dailyRowForToday = DailyRepetitionStatisticReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionStatisticReportRow updatedDailyRow =
                dailyRepetitionStatisticReportRowService.updateRow(dailyRowForToday, testData.data());

        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports()).containsExactly(testData.expectedDictionaryGoalReport());
    }

    @ParameterizedTest
    @MethodSource("buildRowFromScratchTestData")
    void testUpdateRow_WhenDailyRowExistsForTodayAndGivenNewDictionary_ThenRecalculateRow(TestData testData) {
        DictionaryRepetitionStatisticReport dictionaryGoalReport =
                new DictionaryRepetitionStatisticReport(1L, 5L, 96.429, 56, 79.63, 43, 20.37, 11, 3.571, 2, 54, 1);
        DailyRepetitionStatisticReportRow dailyRowForToday = DailyRepetitionStatisticReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate())
                .endPeriod(testData.data().getCurrentDate())
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionStatisticReportRow updatedDailyRow =
                dailyRepetitionStatisticReportRowService.updateRow(dailyRowForToday, testData.data());

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
        DictionaryRepetitionStatisticReport dictionaryGoalReport =
                new DictionaryRepetitionStatisticReport(1L, 10L, 92.81, 153, 77.465, 110, 22.535, 32, 7.19, 11, 142, 2);
        DailyRepetitionStatisticReportRow dailyRowForToday = DailyRepetitionStatisticReportRow.builder()
                .id(1L)
                .startPeriod(testData.data().getCurrentDate().minusDays(1))
                .endPeriod(testData.data().getCurrentDate().minusDays(1))
                .workingDays(1)
                .dictionaryReports(Set.of(dictionaryGoalReport))
                .build();

        RepetitionStatisticReportRow updatedDailyRow =
                dailyRepetitionStatisticReportRowService.updateRow(dailyRowForToday, testData.data());

        assertThat(updatedDailyRow.getWorkingDays()).isEqualTo(1);
        assertThat(updatedDailyRow.getRowPeriod()).isEqualTo(ReportPeriodUtil.DAY);
        assertThat(updatedDailyRow.getStartPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getEndPeriod()).isEqualTo(testData.data().getCurrentDate());
        assertThat(updatedDailyRow.getDictionaryReports())
                .containsExactlyInAnyOrder(testData.expectedDictionaryGoalReport());
    }

    private static Stream<TestData> buildRowFromScratchTestData() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        return Stream.of(
                new TestData(
                        new RepetitionStatisticData(
                                USER_ID, dictionaryId, userCreatedAt, currentDate, 43, 11, 2, 56, 54),
                        new DictionaryRepetitionStatisticReport(
                                null, dictionaryId, 96.429, 56, 79.63, 43, 20.37, 11, 3.571, 2, 54, 1)),
                new TestData(
                        new RepetitionStatisticData(
                                USER_ID, dictionaryId, userCreatedAt, currentDate, 13, 15, 6, 34, 28),
                        new DictionaryRepetitionStatisticReport(
                                null, dictionaryId, 82.353, 34, 46.429, 13, 53.571, 15, 17.647, 6, 28, 1)));
    }

    private static Stream<TestData> updateRowTestData() {
        Long dictionaryId = 10L;
        LocalDate userCreatedAt = LocalDate.of(2024, 7, 9);
        LocalDate currentDate = LocalDate.of(2024, 7, 12);
        return Stream.of(
                new TestData(
                        new RepetitionStatisticData(
                                USER_ID, dictionaryId, userCreatedAt, currentDate, 67, 21, 9, 97, 88),
                        new DictionaryRepetitionStatisticReport(
                                1L, dictionaryId, 92.81, 153, 77.465, 110, 22.535, 32, 7.19, 11, 142, 2)),
                new TestData(
                        new RepetitionStatisticData(
                                USER_ID, dictionaryId, userCreatedAt, currentDate, 10, 23, 5, 38, 33),
                        new DictionaryRepetitionStatisticReport(
                                1L, dictionaryId, 92.553, 94, 60.92, 53, 39.08, 34, 7.447, 7, 87, 2)));
    }

    private record TestData(
            RepetitionStatisticData data, DictionaryRepetitionStatisticReport expectedDictionaryGoalReport) {}
}
