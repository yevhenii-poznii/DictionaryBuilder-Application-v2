package com.kiskee.vocabulary.service.report.goal.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.dto.user.preference.WordPreference;
import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.DailyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.MonthlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.TotalRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.WeeklyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.time.period.YearlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.repository.report.RepetitionTimeSpendGoalReportRepository;
import com.kiskee.vocabulary.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.impl.DailyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.impl.MonthlyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.impl.TotalRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.impl.WeeklyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.time.row.impl.YearlyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.user.preference.WordPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RepetitionTimeSpendGoalReportServiceTest {

    @InjectMocks
    private RepetitionTimeSpendGoalReportService repetitionTimeSpendGoalReportService;

    @Mock
    private RepetitionTimeSpendGoalReportRepository repository;

    @Mock
    private UserProfileInfoProvider userProfileInfoProvider;

    @Mock
    private WordPreferenceService wordPreferenceService;

    @Mock
    private List<RepetitionTimeSpendGoalReportRowService> rowServices;

    @Captor
    private ArgumentCaptor<RepetitionTimeSpendGoalReport> reportCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testUpdateReport_WhenRepetitionTimeSpendGoalReportDoesNotExist_ThenCreateNewReportFromScratch() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData(List.of());

        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Instant userCreatedAtField = Instant.parse("2024-07-08T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);

        setupCreateFromScratch();

        repetitionTimeSpendGoalReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionTimeSpendGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionTimeSpendGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void
            testUpdateReport_WhenRepetitionTimeSpendGoalReportDoesNotExistAndThereArePauses_ThenCreateNewReportFromScratch() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData(List.of(
                new Pause(Instant.parse("2024-07-09T12:50:23Z"), Instant.parse("2024-07-09T12:55:23Z")),
                new Pause(Instant.parse("2024-07-09T13:00:23Z"), Instant.parse("2024-07-09T13:05:23Z"))));

        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Instant userCreatedAtField = Instant.parse("2024-07-08T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);

        setupCreateFromScratch();

        repetitionTimeSpendGoalReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionTimeSpendGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionTimeSpendGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportExists_ThenUpdateExistingReport() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData(List.of());

        RepetitionTimeSpendGoalReport existingReport = mock(RepetitionTimeSpendGoalReport.class);
        when(existingReport.getUserId()).thenReturn(USER_ID);
        Set<RepetitionTimeSpendGoalReportRow> rows = buildRows();
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);

        setupUpdateExistingReport();

        when(existingReport.buildFrom(any())).thenReturn(existingReport);

        repetitionTimeSpendGoalReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionTimeSpendGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionTimeSpendGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportExistsAndThereArePauses_ThenUpdateExistingReport() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData(List.of(
                new Pause(Instant.parse("2024-07-09T12:50:23Z"), Instant.parse("2024-07-09T12:55:23Z")),
                new Pause(Instant.parse("2024-07-09T13:00:23Z"), Instant.parse("2024-07-09T13:05:23Z"))));

        RepetitionTimeSpendGoalReport existingReport = mock(RepetitionTimeSpendGoalReport.class);
        when(existingReport.getUserId()).thenReturn(USER_ID);
        Set<RepetitionTimeSpendGoalReportRow> rows = buildRows();
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);

        setupUpdateExistingReport();

        when(existingReport.buildFrom(any())).thenReturn(existingReport);

        repetitionTimeSpendGoalReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionTimeSpendGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionTimeSpendGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    private void setupCreateFromScratch() {
        DailyRepetitionTimeSpendGoalReportRowService dailyRowService =
                mock(DailyRepetitionTimeSpendGoalReportRowService.class);
        DailyRepetitionTimeSpendGoalReportRow dailyRow = mock(DailyRepetitionTimeSpendGoalReportRow.class);
        when(dailyRowService.buildRowFromScratch(any())).thenReturn(dailyRow);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyRepetitionTimeSpendGoalReportRowService weeklyRowService =
                mock(WeeklyRepetitionTimeSpendGoalReportRowService.class);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRow = mock(WeeklyRepetitionTimeSpendGoalReportRow.class);
        when(weeklyRowService.buildRowFromScratch(any())).thenReturn(weeklyRow);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyRepetitionTimeSpendGoalReportRowService monthlyRowService =
                mock(MonthlyRepetitionTimeSpendGoalReportRowService.class);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRow = mock(MonthlyRepetitionTimeSpendGoalReportRow.class);
        when(monthlyRowService.buildRowFromScratch(any())).thenReturn(monthlyRow);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyRepetitionTimeSpendGoalReportRowService yearlyRowService =
                mock(YearlyRepetitionTimeSpendGoalReportRowService.class);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRow = mock(YearlyRepetitionTimeSpendGoalReportRow.class);
        when(yearlyRowService.buildRowFromScratch(any())).thenReturn(yearlyRow);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalRepetitionTimeSpendGoalReportRowService totalRowService =
                mock(TotalRepetitionTimeSpendGoalReportRowService.class);
        TotalRepetitionTimeSpendGoalReportRow totalRow = mock(TotalRepetitionTimeSpendGoalReportRow.class);
        when(totalRowService.buildRowFromScratch(any())).thenReturn(totalRow);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        List<RepetitionTimeSpendGoalReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupUpdateExistingReport() {
        DailyRepetitionTimeSpendGoalReportRowService dailyRowService =
                mock(DailyRepetitionTimeSpendGoalReportRowService.class);
        DailyRepetitionTimeSpendGoalReportRow dailyRow = mock(DailyRepetitionTimeSpendGoalReportRow.class);
        when(dailyRowService.updateRow(any(), any())).thenReturn(dailyRow);

        WeeklyRepetitionTimeSpendGoalReportRowService weeklyRowService =
                mock(WeeklyRepetitionTimeSpendGoalReportRowService.class);
        WeeklyRepetitionTimeSpendGoalReportRow weeklyRow = mock(WeeklyRepetitionTimeSpendGoalReportRow.class);
        when(weeklyRowService.updateRow(any(), any())).thenReturn(weeklyRow);

        MonthlyRepetitionTimeSpendGoalReportRowService monthlyRowService =
                mock(MonthlyRepetitionTimeSpendGoalReportRowService.class);
        MonthlyRepetitionTimeSpendGoalReportRow monthlyRow = mock(MonthlyRepetitionTimeSpendGoalReportRow.class);
        when(monthlyRowService.updateRow(any(), any())).thenReturn(monthlyRow);

        YearlyRepetitionTimeSpendGoalReportRowService yearlyRowService =
                mock(YearlyRepetitionTimeSpendGoalReportRowService.class);
        YearlyRepetitionTimeSpendGoalReportRow yearlyRow = mock(YearlyRepetitionTimeSpendGoalReportRow.class);
        when(yearlyRowService.updateRow(any(), any())).thenReturn(yearlyRow);

        TotalRepetitionTimeSpendGoalReportRowService totalRowService =
                mock(TotalRepetitionTimeSpendGoalReportRowService.class);
        TotalRepetitionTimeSpendGoalReportRow totalRow = mock(TotalRepetitionTimeSpendGoalReportRow.class);
        when(totalRowService.updateRow(any(), any())).thenReturn(totalRow);

        List<RepetitionTimeSpendGoalReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupRowServices(List<RepetitionTimeSpendGoalReportRowService> rowServices) {
        ReflectionTestUtils.setField(repetitionTimeSpendGoalReportService, "rowServices", rowServices);
    }

    private Set<RepetitionTimeSpendGoalReportRow> buildRows() {
        DailyRepetitionTimeSpendGoalReportRow dailyRow = mock(DailyRepetitionTimeSpendGoalReportRow.class);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyRepetitionTimeSpendGoalReportRow weeklyRow = mock(WeeklyRepetitionTimeSpendGoalReportRow.class);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyRepetitionTimeSpendGoalReportRow monthlyRow = mock(MonthlyRepetitionTimeSpendGoalReportRow.class);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyRepetitionTimeSpendGoalReportRow yearlyRow = mock(YearlyRepetitionTimeSpendGoalReportRow.class);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalRepetitionTimeSpendGoalReportRow totalRow = mock(TotalRepetitionTimeSpendGoalReportRow.class);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        return Set.of(dailyRow, weeklyRow, monthlyRow, yearlyRow, totalRow);
    }

    private RepetitionResultData buildRepetitionResultData(List<Pause> pauses) {
        return RepetitionResultDataDto.builder()
                .userId(USER_ID)
                .dictionaryId(10L)
                .startTime(Instant.parse("2024-07-09T12:48:23Z"))
                .endTime(Instant.parse("2024-07-09T13:15:18Z"))
                .pauses(pauses)
                .userTimeZone(ZoneId.of("Asia/Tokyo"))
                .build();
    }
}
