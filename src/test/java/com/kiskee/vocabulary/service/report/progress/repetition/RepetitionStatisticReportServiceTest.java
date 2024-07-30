package com.kiskee.vocabulary.service.report.progress.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.model.dto.repetition.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.DailyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.MonthlyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.TotalRepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.WeeklyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.period.YearlyRepetitionStatisticReportRow;
import com.kiskee.vocabulary.repository.report.RepetitionStatisticReportRepository;
import com.kiskee.vocabulary.service.report.progress.repetition.row.RepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.impl.DailyRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.impl.MonthlyRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.impl.TotalRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.impl.WeeklyRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.impl.YearlyRepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
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
public class RepetitionStatisticReportServiceTest {

    @InjectMocks
    private RepetitionStatisticReportService repetitionStatisticReportService;

    @Mock
    private RepetitionStatisticReportRepository repository;

    @Mock
    private UserProfileInfoProvider userProfileInfoProvider;

    @Mock
    private List<RepetitionStatisticReportRowService> rowServices;

    @Captor
    private ArgumentCaptor<RepetitionStatisticReport> reportCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportDoesNotExist_ThenCreateNewReportFromScratch() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData();

        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        setupCreateFromScratch();

        repetitionStatisticReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionStatisticReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionStatisticReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportExists_ThenUpdateExistingReport() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData();

        RepetitionStatisticReport existingReport = mock(RepetitionStatisticReport.class);
        when(existingReport.getUserId()).thenReturn(USER_ID);
        Set<RepetitionStatisticReportRow> rows = buildRows();
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        setupUpdateExistingReport();

        when(existingReport.buildFrom(any())).thenReturn(existingReport);

        repetitionStatisticReportService.updateReport(repetitionResultData);

        verify(repository).save(reportCaptor.capture());
        RepetitionStatisticReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(RepetitionStatisticReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    private void setupCreateFromScratch() {
        DailyRepetitionStatisticReportRowService dailyRowService = mock(DailyRepetitionStatisticReportRowService.class);
        DailyRepetitionStatisticReportRow dailyRow = mock(DailyRepetitionStatisticReportRow.class);
        when(dailyRowService.buildRowFromScratch(any())).thenReturn(dailyRow);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyRepetitionStatisticReportRowService weeklyRowService =
                mock(WeeklyRepetitionStatisticReportRowService.class);
        WeeklyRepetitionStatisticReportRow weeklyRow = mock(WeeklyRepetitionStatisticReportRow.class);
        when(weeklyRowService.buildRowFromScratch(any())).thenReturn(weeklyRow);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyRepetitionStatisticReportRowService monthlyRowService =
                mock(MonthlyRepetitionStatisticReportRowService.class);
        MonthlyRepetitionStatisticReportRow monthlyRow = mock(MonthlyRepetitionStatisticReportRow.class);
        when(monthlyRowService.buildRowFromScratch(any())).thenReturn(monthlyRow);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyRepetitionStatisticReportRowService yearlyRowService =
                mock(YearlyRepetitionStatisticReportRowService.class);
        YearlyRepetitionStatisticReportRow yearlyRow = mock(YearlyRepetitionStatisticReportRow.class);
        when(yearlyRowService.buildRowFromScratch(any())).thenReturn(yearlyRow);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalRepetitionStatisticReportRowService totalRowService = mock(TotalRepetitionStatisticReportRowService.class);
        TotalRepetitionStatisticReportRow totalRow = mock(TotalRepetitionStatisticReportRow.class);
        when(totalRowService.buildRowFromScratch(any())).thenReturn(totalRow);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        List<RepetitionStatisticReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupUpdateExistingReport() {
        DailyRepetitionStatisticReportRowService dailyRowService = mock(DailyRepetitionStatisticReportRowService.class);
        DailyRepetitionStatisticReportRow dailyRow = mock(DailyRepetitionStatisticReportRow.class);
        when(dailyRowService.updateRow(any(), any())).thenReturn(dailyRow);

        WeeklyRepetitionStatisticReportRowService weeklyRowService =
                mock(WeeklyRepetitionStatisticReportRowService.class);
        WeeklyRepetitionStatisticReportRow weeklyRow = mock(WeeklyRepetitionStatisticReportRow.class);
        when(weeklyRowService.updateRow(any(), any())).thenReturn(weeklyRow);

        MonthlyRepetitionStatisticReportRowService monthlyRowService =
                mock(MonthlyRepetitionStatisticReportRowService.class);
        MonthlyRepetitionStatisticReportRow monthlyRow = mock(MonthlyRepetitionStatisticReportRow.class);
        when(monthlyRowService.updateRow(any(), any())).thenReturn(monthlyRow);

        YearlyRepetitionStatisticReportRowService yearlyRowService =
                mock(YearlyRepetitionStatisticReportRowService.class);
        YearlyRepetitionStatisticReportRow yearlyRow = mock(YearlyRepetitionStatisticReportRow.class);
        when(yearlyRowService.updateRow(any(), any())).thenReturn(yearlyRow);

        TotalRepetitionStatisticReportRowService totalRowService = mock(TotalRepetitionStatisticReportRowService.class);
        TotalRepetitionStatisticReportRow totalRow = mock(TotalRepetitionStatisticReportRow.class);
        when(totalRowService.updateRow(any(), any())).thenReturn(totalRow);

        List<RepetitionStatisticReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupRowServices(List<RepetitionStatisticReportRowService> rowServices) {
        ReflectionTestUtils.setField(repetitionStatisticReportService, "rowServices", rowServices);
    }

    private Set<RepetitionStatisticReportRow> buildRows() {
        DailyRepetitionStatisticReportRow dailyRow = mock(DailyRepetitionStatisticReportRow.class);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyRepetitionStatisticReportRow weeklyRow = mock(WeeklyRepetitionStatisticReportRow.class);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyRepetitionStatisticReportRow monthlyRow = mock(MonthlyRepetitionStatisticReportRow.class);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyRepetitionStatisticReportRow yearlyRow = mock(YearlyRepetitionStatisticReportRow.class);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalRepetitionStatisticReportRow totalRow = mock(TotalRepetitionStatisticReportRow.class);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        return Set.of(dailyRow, weeklyRow, monthlyRow, yearlyRow, totalRow);
    }

    private RepetitionResultData buildRepetitionResultData() {
        return RepetitionResultDataDto.builder()
                .userId(USER_ID)
                .dictionaryId(10L)
                .endTime(Instant.parse("2024-07-09T13:15:18Z"))
                .userTimeZone(ZoneId.of("Asia/Tokyo"))
                .build();
    }
}
