package com.kiskee.dictionarybuilder.service.report.goal.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kiskee.dictionarybuilder.exception.ResourceNotFoundException;
import com.kiskee.dictionarybuilder.mapper.report.RepetitionTimeSpendGoalReportMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultData;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultDataDto;
import com.kiskee.dictionarybuilder.model.dto.report.BaseReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.ReportRowDto;
import com.kiskee.dictionarybuilder.model.dto.report.goal.time.RepetitionTimeSpendGoalReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.update.PeriodRange;
import com.kiskee.dictionarybuilder.model.dto.user.preference.WordPreference;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.Pause;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.DailyRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.MonthlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.TotalRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.WeeklyRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.period.YearlyRepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.model.entity.user.UserVocabularyApplication;
import com.kiskee.dictionarybuilder.repository.report.RepetitionTimeSpendGoalReportRepository;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.impl.DailyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.impl.MonthlyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.impl.TotalRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.impl.WeeklyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.impl.YearlyRepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.user.preference.WordPreferenceService;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileInfoProvider;
import com.kiskee.dictionarybuilder.util.TimeZoneContextHolder;
import com.kiskee.dictionarybuilder.util.report.ReportPeriodUtil;
import java.time.Duration;
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
public class RepetitionTimeSpendGoalReportServiceTest {

    @InjectMocks
    private RepetitionTimeSpendGoalReportService repetitionTimeSpendGoalReportService;

    @Mock
    private RepetitionTimeSpendGoalReportRepository repository;

    @Mock
    private RepetitionTimeSpendGoalReportMapper mapper;

    @Mock
    private UserProfileInfoProvider userProfileInfoProvider;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Mock
    private WordPreferenceService wordPreferenceService;

    @Mock
    private List<RepetitionTimeSpendGoalReportRowService> rowServices;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<RepetitionTimeSpendGoalReport> reportCaptor;

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

        RepetitionTimeSpendGoalReport existingReport = mock(RepetitionTimeSpendGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate, currentDate);
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate);
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate);
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        Set<RepetitionTimeSpendGoalReportRow> rows = Set.of(
                new DailyRepetitionTimeSpendGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionTimeSpendGoalReportRow(weekPeriodRange, 4, Set.of()),
                new MonthlyRepetitionTimeSpendGoalReportRow(monthPeriodRange, 11, Set.of()),
                new YearlyRepetitionTimeSpendGoalReportRow(yearPeriodRange, 54, Set.of()),
                new TotalRepetitionTimeSpendGoalReportRow(totalPeriodRange, 54, Set.of()));
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
        when(mapper.toDto(new RepetitionTimeSpendGoalReport(USER_ID, rows)))
                .thenReturn(new RepetitionTimeSpendGoalReportDto(rowDtoSet));

        ReportDto reportDto = repetitionTimeSpendGoalReportService.getReport();

        verify(mapper).toDto(new RepetitionTimeSpendGoalReport(USER_ID, rows));

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

        RepetitionTimeSpendGoalReport existingReport = mock(RepetitionTimeSpendGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(1), currentDate.minusDays(1));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate.minusDays(1));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(1));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        Set<RepetitionTimeSpendGoalReportRow> rows = Set.of(
                new DailyRepetitionTimeSpendGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionTimeSpendGoalReportRow(weekPeriodRange, 3, Set.of()),
                new MonthlyRepetitionTimeSpendGoalReportRow(monthPeriodRange, 10, Set.of()),
                new YearlyRepetitionTimeSpendGoalReportRow(yearPeriodRange, 53, Set.of()),
                new TotalRepetitionTimeSpendGoalReportRow(totalPeriodRange, 53, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<RepetitionTimeSpendGoalReportRow> filteredRows = rows.stream()
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
        when(mapper.toDto(new RepetitionTimeSpendGoalReport(USER_ID, filteredRows)))
                .thenReturn(new RepetitionTimeSpendGoalReportDto(rowDtoSet));

        ReportDto reportDto = repetitionTimeSpendGoalReportService.getReport();

        verify(mapper).toDto(new RepetitionTimeSpendGoalReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.WEEK, ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportExistsAndDailyAndWeeklyRowNotInCurrentPeriod_ThenReturnReportDto() {
        setAuth();

        RepetitionTimeSpendGoalReport existingReport = mock(RepetitionTimeSpendGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(6), currentDate.minusDays(6));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(9), currentDate.minusDays(6));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(6));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        Set<RepetitionTimeSpendGoalReportRow> rows = Set.of(
                new DailyRepetitionTimeSpendGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyRepetitionTimeSpendGoalReportRow(weekPeriodRange, 2, Set.of()),
                new MonthlyRepetitionTimeSpendGoalReportRow(monthPeriodRange, 5, Set.of()),
                new YearlyRepetitionTimeSpendGoalReportRow(yearPeriodRange, 47, Set.of()),
                new TotalRepetitionTimeSpendGoalReportRow(totalPeriodRange, 47, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<RepetitionTimeSpendGoalReportRow> filteredRows = rows.stream()
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
        when(mapper.toDto(new RepetitionTimeSpendGoalReport(USER_ID, filteredRows)))
                .thenReturn(new RepetitionTimeSpendGoalReportDto(rowDtoSet));

        ReportDto reportDto = repetitionTimeSpendGoalReportService.getReport();

        verify(mapper).toDto(new RepetitionTimeSpendGoalReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportDoesNotExist_ThenReturnReportDto() {
        setAuth();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> repetitionTimeSpendGoalReportService.getReport())
                .withMessage("There is no report yet");
    }

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
    void testUpdateReport_WhenRepetitionTimeSpendGoalReportExists_ThenUpdateExistingReport() {
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
    void testUpdateReport_WhenRepetitionTimeSpendGoalReportExistsAndThereArePauses_ThenUpdateExistingReport() {
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

    @Test
    void testUpdateReport_WhenThrownExceptionDuring_ThenThrowException() {
        RepetitionResultData repetitionResultData = buildRepetitionResultData(List.of());

        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(null);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> repetitionTimeSpendGoalReportService.updateReport(repetitionResultData));
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportRepository() {
        assertThat(repetitionTimeSpendGoalReportService.getRepository()).isEqualTo(repository);
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportMapper() {
        assertThat(repetitionTimeSpendGoalReportService.getMapper()).isEqualTo(mapper);
    }

    @Test
    void testGetUserProfileInfoProvider() {
        assertThat(repetitionTimeSpendGoalReportService.getUserProfileInfoProvider())
                .isEqualTo(userProfileInfoProvider);
    }

    @Test
    void testGetWordPreferenceService() {
        assertThat(repetitionTimeSpendGoalReportService.getWordPreferenceService())
                .isEqualTo(wordPreferenceService);
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
