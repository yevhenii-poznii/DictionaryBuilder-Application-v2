package com.kiskee.dictionarybuilder.service.report.progress.repetition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.mapper.report.RepetitionStatisticReportMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultData;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.dictionarybuilder.model.dto.report.BaseReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.progress.RepetitionStatisticReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.RepetitionStatisticReport;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.DailyRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.MonthlyRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.TotalRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.WeeklyRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.progress.repetition.period.YearlyRepetitionStatisticReportRow;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.report.RepetitionStatisticReportRepository;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.RepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl.DailyRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl.MonthlyRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl.TotalRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl.WeeklyRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.row.impl.YearlyRepetitionStatisticReportRowService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileInfoProvider;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RepetitionStatisticReportServiceTest {

    @InjectMocks
    private RepetitionStatisticReportService repetitionStatisticReportService;

    @Mock
    private RepetitionStatisticReportRepository repository;

    @Mock
    private RepetitionStatisticReportMapper mapper;

    @Mock
    private UserProfileInfoProvider userProfileInfoProvider;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Mock
    private List<RepetitionStatisticReportRowService> rowServices;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<RepetitionStatisticReport> reportCaptor;

    private static final UUID USER_ID = UUID.fromString("78c87bb3-01b6-41ca-8329-247a72162868");

    @BeforeEach
    void setUp() {
        TimeZoneContextHolder.setTimeZone("Asia/Tokyo");
    }

    @AfterEach
    void tearDown() {
        TimeZoneContextHolder.clear();
    }

    @Test
    void testGetReport_WhenReportExistsAndAllRowsInCurrentPeriod_ThenReturnReportDto() {
        setAuth();

        RepetitionStatisticReport existingReport = mock(RepetitionStatisticReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate, currentDate);
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate);
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate);
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        Set<RepetitionStatisticReportRow> rows = Set.of(
                new DailyRepetitionStatisticReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionStatisticReportRow(weekPeriodRange, 4, Set.of()),
                new MonthlyRepetitionStatisticReportRow(monthPeriodRange, 11, Set.of()),
                new YearlyRepetitionStatisticReportRow(yearPeriodRange, 54, Set.of()),
                new TotalRepetitionStatisticReportRow(totalPeriodRange, 54, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        List<ReportRowDto> rowDtoSet = List.of(
                new BaseReportRowDto(
                        dayPeriodRange.startPeriod(), dayPeriodRange.endPeriod(), 1, ReportPeriodUtil.DAY, List.of()),
                new BaseReportRowDto(
                        weekPeriodRange.startPeriod(),
                        weekPeriodRange.endPeriod(),
                        3,
                        ReportPeriodUtil.WEEK,
                        List.of()),
                new BaseReportRowDto(
                        monthPeriodRange.startPeriod(),
                        monthPeriodRange.endPeriod(),
                        10,
                        ReportPeriodUtil.MONTH,
                        List.of()),
                new BaseReportRowDto(
                        yearPeriodRange.startPeriod(),
                        yearPeriodRange.endPeriod(),
                        54,
                        ReportPeriodUtil.YEAR,
                        List.of()),
                new BaseReportRowDto(
                        totalPeriodRange.startPeriod(),
                        totalPeriodRange.endPeriod(),
                        54,
                        ReportPeriodUtil.TOTAL,
                        List.of()));
        when(mapper.toDto(new RepetitionStatisticReport(USER_ID, rows)))
                .thenReturn(new RepetitionStatisticReportDto(rowDtoSet));

        ReportDto reportDto = repetitionStatisticReportService.getReport();

        verify(mapper).toDto(new RepetitionStatisticReport(USER_ID, rows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportExistsAndDailyRowNotInCurrentPeriod_ThenReturnReportDto() {
        setAuth();

        RepetitionStatisticReport existingReport = mock(RepetitionStatisticReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(1), currentDate.minusDays(1));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate.minusDays(1));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(1));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        Set<RepetitionStatisticReportRow> rows = Set.of(
                new DailyRepetitionStatisticReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionStatisticReportRow(weekPeriodRange, 3, Set.of()),
                new MonthlyRepetitionStatisticReportRow(monthPeriodRange, 10, Set.of()),
                new YearlyRepetitionStatisticReportRow(yearPeriodRange, 53, Set.of()),
                new TotalRepetitionStatisticReportRow(totalPeriodRange, 53, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<RepetitionStatisticReportRow> filteredRows = rows.stream()
                .filter(row -> !row.getRowPeriod().equals(ReportPeriodUtil.DAY))
                .collect(Collectors.toSet());

        List<ReportRowDto> rowDtoSet = List.of(
                new BaseReportRowDto(
                        weekPeriodRange.startPeriod(),
                        weekPeriodRange.endPeriod(),
                        3,
                        ReportPeriodUtil.WEEK,
                        List.of()),
                new BaseReportRowDto(
                        monthPeriodRange.startPeriod(),
                        monthPeriodRange.endPeriod(),
                        10,
                        ReportPeriodUtil.MONTH,
                        List.of()),
                new BaseReportRowDto(
                        yearPeriodRange.startPeriod(),
                        yearPeriodRange.endPeriod(),
                        54,
                        ReportPeriodUtil.YEAR,
                        List.of()),
                new BaseReportRowDto(
                        totalPeriodRange.startPeriod(),
                        totalPeriodRange.endPeriod(),
                        54,
                        ReportPeriodUtil.TOTAL,
                        List.of()));
        when(mapper.toDto(new RepetitionStatisticReport(USER_ID, filteredRows)))
                .thenReturn(new RepetitionStatisticReportDto(rowDtoSet));

        ReportDto reportDto = repetitionStatisticReportService.getReport();

        verify(mapper).toDto(new RepetitionStatisticReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.WEEK, ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportExistsAndDailyAndWeeklyRowNotInCurrentPeriod_ThenReturnReportDto() {
        setAuth();

        RepetitionStatisticReport existingReport = mock(RepetitionStatisticReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(6), currentDate.minusDays(6));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(9), currentDate.minusDays(6));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(6));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        Set<RepetitionStatisticReportRow> rows = Set.of(
                new DailyRepetitionStatisticReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionStatisticReportRow(weekPeriodRange, 2, Set.of()),
                new MonthlyRepetitionStatisticReportRow(monthPeriodRange, 5, Set.of()),
                new YearlyRepetitionStatisticReportRow(yearPeriodRange, 47, Set.of()),
                new TotalRepetitionStatisticReportRow(totalPeriodRange, 47, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<RepetitionStatisticReportRow> filteredRows = rows.stream()
                .filter(row -> !row.getRowPeriod().equals(ReportPeriodUtil.DAY)
                        && !row.getRowPeriod().equals(ReportPeriodUtil.WEEK))
                .collect(Collectors.toSet());

        List<ReportRowDto> rowDtoSet = List.of(
                new BaseReportRowDto(
                        monthPeriodRange.startPeriod(),
                        monthPeriodRange.endPeriod(),
                        5,
                        ReportPeriodUtil.MONTH,
                        List.of()),
                new BaseReportRowDto(
                        yearPeriodRange.startPeriod(),
                        yearPeriodRange.endPeriod(),
                        47,
                        ReportPeriodUtil.YEAR,
                        List.of()),
                new BaseReportRowDto(
                        totalPeriodRange.startPeriod(),
                        totalPeriodRange.endPeriod(),
                        47,
                        ReportPeriodUtil.TOTAL,
                        List.of()));
        when(mapper.toDto(new RepetitionStatisticReport(USER_ID, filteredRows)))
                .thenReturn(new RepetitionStatisticReportDto(rowDtoSet));

        ReportDto reportDto = repetitionStatisticReportService.getReport();

        verify(mapper).toDto(new RepetitionStatisticReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportDoesNotExist_ThenReturnReportDto() {
        setAuth();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> repetitionStatisticReportService.getReport())
                .withMessage("There is no report yet");
    }

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

    @Test
    void testUpdateReport_WhenThrownExceptionDuring_ThenThrowException() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData();

        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(null);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> repetitionStatisticReportService.updateReport(repetitionResultData));
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportRepository() {
        assertThat(repetitionStatisticReportService.getRepository()).isEqualTo(repository);
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportMapper() {
        assertThat(repetitionStatisticReportService.getMapper()).isEqualTo(mapper);
    }

    @Test
    void testGetUserProfileInfoProvider() {
        assertThat(repetitionStatisticReportService.getUserProfileInfoProvider())
                .isEqualTo(userProfileInfoProvider);
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

    private void setAuth() {
        UserVocabularyApplication user = UserVocabularyApplication.builder()
                .setId(USER_ID)
                .setUsername("username")
                .build();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}
