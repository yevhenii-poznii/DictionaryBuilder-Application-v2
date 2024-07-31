package com.kiskee.vocabulary.service.report.goal.word;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kiskee.vocabulary.exception.ResourceNotFoundException;
import com.kiskee.vocabulary.mapper.report.WordAdditionGoalReportMapper;
import com.kiskee.vocabulary.model.dto.report.BaseReportRowDto;
import com.kiskee.vocabulary.model.dto.report.ReportDto;
import com.kiskee.vocabulary.model.dto.report.ReportRowDto;
import com.kiskee.vocabulary.model.dto.report.goal.word.WordAdditionGoalReportDto;
import com.kiskee.vocabulary.model.dto.report.update.PeriodRange;
import com.kiskee.vocabulary.model.dto.user.preference.WordPreference;
import com.kiskee.vocabulary.model.dto.vocabulary.dictionary.DictionaryDto;
import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.DailyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.MonthlyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.TotalWordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.WeeklyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.report.goal.word.period.YearlyWordAdditionGoalReportRow;
import com.kiskee.vocabulary.model.entity.user.UserVocabularyApplication;
import com.kiskee.vocabulary.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.vocabulary.repository.report.WordAdditionGoalReportRepository;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.impl.DailyWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.impl.MonthlyWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.impl.TotalWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.impl.WeeklyWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.report.goal.word.row.impl.YearlyWordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.time.CurrentDateTimeService;
import com.kiskee.vocabulary.service.user.preference.WordPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import com.kiskee.vocabulary.service.vocabulary.dictionary.DictionaryAccessValidator;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import com.kiskee.vocabulary.util.report.ReportPeriodUtil;
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
public class WordAdditionGoalReportServiceTest {

    @InjectMocks
    private WordAdditionGoalReportService wordAdditionGoalReportService;

    @Mock
    private TemporaryWordAdditionCacheRepository temporaryWordAdditionCacheRepository;

    @Mock
    private WordAdditionGoalReportRepository repository;

    @Mock
    private WordAdditionGoalReportMapper mapper;

    @Mock
    private UserProfileInfoProvider userProfileInfoProvider;

    @Mock
    private WordPreferenceService wordPreferenceService;

    @Mock
    private DictionaryAccessValidator dictionaryAccessValidator;

    @Mock
    private List<WordAdditionGoalReportRowService> rowServices;

    @Mock
    private CurrentDateTimeService currentDateTimeService;

    @Mock
    private SecurityContext securityContext;

    @Captor
    private ArgumentCaptor<WordAdditionGoalReport> reportCaptor;

    private static final String KEY = "78c87bb3-01b6-41ca-8329-247a72162868:10L:2024-07-12";
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

        WordAdditionGoalReport existingReport = mock(WordAdditionGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate, currentDate);
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate);
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate);
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate);
        Set<WordAdditionGoalReportRow> rows = Set.of(
                new DailyWordAdditionGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyWordAdditionGoalReportRow(weekPeriodRange, 4, Set.of()),
                new MonthlyWordAdditionGoalReportRow(monthPeriodRange, 11, Set.of()),
                new YearlyWordAdditionGoalReportRow(yearPeriodRange, 54, Set.of()),
                new TotalWordAdditionGoalReportRow(totalPeriodRange, 54, Set.of()));
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
        when(mapper.toDto(new WordAdditionGoalReport(USER_ID, rows)))
                .thenReturn(new WordAdditionGoalReportDto(rowDtoSet));

        ReportDto reportDto = wordAdditionGoalReportService.getReport();

        verify(mapper).toDto(new WordAdditionGoalReport(USER_ID, rows));

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

        WordAdditionGoalReport existingReport = mock(WordAdditionGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(1), currentDate.minusDays(1));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(3), currentDate.minusDays(1));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(1));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(1));
        Set<WordAdditionGoalReportRow> rows = Set.of(
                new DailyWordAdditionGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyWordAdditionGoalReportRow(weekPeriodRange, 3, Set.of()),
                new MonthlyWordAdditionGoalReportRow(monthPeriodRange, 10, Set.of()),
                new YearlyWordAdditionGoalReportRow(yearPeriodRange, 53, Set.of()),
                new TotalWordAdditionGoalReportRow(totalPeriodRange, 53, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<WordAdditionGoalReportRow> filteredRows = rows.stream()
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
        when(mapper.toDto(new WordAdditionGoalReport(USER_ID, filteredRows)))
                .thenReturn(new WordAdditionGoalReportDto(rowDtoSet));

        ReportDto reportDto = wordAdditionGoalReportService.getReport();

        verify(mapper).toDto(new WordAdditionGoalReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.WEEK, ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportExistsAndDailyAndWeeklyRowNotInCurrentPeriod_ThenReturnReportDto() {
        setAuth();

        WordAdditionGoalReport existingReport = mock(WordAdditionGoalReport.class);
        LocalDate currentDate = LocalDate.of(2024, 7, 11);
        when(currentDateTimeService.getCurrentDate(any(ZoneId.class))).thenReturn(currentDate);

        PeriodRange dayPeriodRange = new PeriodRange(currentDate.minusDays(6), currentDate.minusDays(6));
        PeriodRange weekPeriodRange = new PeriodRange(currentDate.minusDays(9), currentDate.minusDays(6));
        PeriodRange monthPeriodRange = new PeriodRange(currentDate.minusDays(10), currentDate.minusDays(6));
        PeriodRange yearPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        PeriodRange totalPeriodRange = new PeriodRange(currentDate.minusDays(53), currentDate.minusDays(6));
        Set<WordAdditionGoalReportRow> rows = Set.of(
                new DailyWordAdditionGoalReportRow(dayPeriodRange, 1, Set.of()),
                new WeeklyWordAdditionGoalReportRow(weekPeriodRange, 2, Set.of()),
                new MonthlyWordAdditionGoalReportRow(monthPeriodRange, 5, Set.of()),
                new YearlyWordAdditionGoalReportRow(yearPeriodRange, 47, Set.of()),
                new TotalWordAdditionGoalReportRow(totalPeriodRange, 47, Set.of()));
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Set<WordAdditionGoalReportRow> filteredRows = rows.stream()
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
        when(mapper.toDto(new WordAdditionGoalReport(USER_ID, filteredRows)))
                .thenReturn(new WordAdditionGoalReportDto(rowDtoSet));

        ReportDto reportDto = wordAdditionGoalReportService.getReport();

        verify(mapper).toDto(new WordAdditionGoalReport(USER_ID, filteredRows));

        assertThat(reportDto.getReportRows())
                .extracting(ReportRowDto::getReportPeriod)
                .containsExactlyInAnyOrder(ReportPeriodUtil.MONTH, ReportPeriodUtil.YEAR, ReportPeriodUtil.TOTAL);
    }

    @Test
    void testGetReport_WhenReportDoesNotExist_ThenReturnReportDto() {
        setAuth();
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> wordAdditionGoalReportService.getReport())
                .withMessage("There is no report yet");
    }

    @Test
    void testUpdateReport_WhenTemporaryWordAdditionDataIsNotFound_ThenDoNothing() {
        when(temporaryWordAdditionCacheRepository.findById(KEY)).thenReturn(Optional.empty());

        wordAdditionGoalReportService.updateReport(KEY);

        verifyNoInteractions(repository);
        verifyNoInteractions(userProfileInfoProvider);
        verifyNoInteractions(wordPreferenceService);
        verifyNoInteractions(rowServices);
    }

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportDoesNotExist_ThenCreateNewReportFromScratch() {
        TemporaryWordAdditionData temporaryWordAdditionData = buildTemporaryWordAdditionData();
        when(temporaryWordAdditionCacheRepository.findById(KEY)).thenReturn(Optional.of(temporaryWordAdditionData));

        when(repository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);
        DictionaryDto dictionaryDto =
                new DictionaryDto(temporaryWordAdditionData.getDictionaryId(), "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(temporaryWordAdditionData.getDictionaryId(), USER_ID))
                .thenReturn(dictionaryDto);

        setupCreateFromScratch();

        wordAdditionGoalReportService.updateReport(KEY);

        verify(repository).save(reportCaptor.capture());
        WordAdditionGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(WordAdditionGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testUpdateReport_WhenWordAdditionGoalReportExists_ThenUpdateExistingReport() {
        TemporaryWordAdditionData temporaryWordAdditionData = buildTemporaryWordAdditionData();
        when(temporaryWordAdditionCacheRepository.findById(KEY)).thenReturn(Optional.of(temporaryWordAdditionData));

        WordAdditionGoalReport existingReport = mock(WordAdditionGoalReport.class);
        when(existingReport.getUserId()).thenReturn(USER_ID);
        Set<WordAdditionGoalReportRow> rows = buildRows();
        when(existingReport.getReportRows()).thenReturn(rows);
        when(repository.findByUserId(USER_ID)).thenReturn(Optional.of(existingReport));

        Instant userCreatedAtField = Instant.parse("2024-07-09T12:48:23Z");
        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(userCreatedAtField);

        WordPreference wordPreference = new WordPreference(10, 10, Duration.ofHours(1));
        when(wordPreferenceService.getWordPreference(USER_ID)).thenReturn(wordPreference);
        DictionaryDto dictionaryDto =
                new DictionaryDto(temporaryWordAdditionData.getDictionaryId(), "SomeDictionaryName");
        when(dictionaryAccessValidator.getDictionaryByIdAndUserId(temporaryWordAdditionData.getDictionaryId(), USER_ID))
                .thenReturn(dictionaryDto);

        setupUpdateExistingReport();

        when(existingReport.buildFrom(any())).thenReturn(existingReport);

        wordAdditionGoalReportService.updateReport(KEY);

        verify(repository).save(reportCaptor.capture());
        WordAdditionGoalReport report = reportCaptor.getValue();
        assertThat(report.getUserId()).isEqualTo(USER_ID);
        assertThat(report.getReportRows())
                .extracting(WordAdditionGoalReportRow::getRowPeriod)
                .containsExactlyInAnyOrder(
                        ReportPeriodUtil.DAY,
                        ReportPeriodUtil.WEEK,
                        ReportPeriodUtil.MONTH,
                        ReportPeriodUtil.YEAR,
                        ReportPeriodUtil.TOTAL);
    }

    @Test
    void testUpdateReport_WhenThrownExceptionDuring_ThenThrowException() {
        TemporaryWordAdditionData temporaryWordAdditionData = buildTemporaryWordAdditionData();
        when(temporaryWordAdditionCacheRepository.findById(KEY)).thenReturn(Optional.of(temporaryWordAdditionData));

        when(userProfileInfoProvider.getCreatedAtField(USER_ID)).thenReturn(null);

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> wordAdditionGoalReportService.updateReport(KEY));
    }

    @Test
    void testGetTemporaryWordAdditionCacheRepository() {
        assertThat(wordAdditionGoalReportService.getTemporaryWordAdditionCacheRepository())
                .isEqualTo(temporaryWordAdditionCacheRepository);
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportRepository() {
        assertThat(wordAdditionGoalReportService.getRepository()).isEqualTo(repository);
    }

    @Test
    void testGetRepetitionTimeSpendGoalReportMapper() {
        assertThat(wordAdditionGoalReportService.getMapper()).isEqualTo(mapper);
    }

    @Test
    void testGetUserProfileInfoProvider() {
        assertThat(wordAdditionGoalReportService.getUserProfileInfoProvider()).isEqualTo(userProfileInfoProvider);
    }

    @Test
    void testGetWordPreferenceService() {
        assertThat(wordAdditionGoalReportService.getWordPreferenceService()).isEqualTo(wordPreferenceService);
    }

    @Test
    void testGetDictionaryAccessValidator() {
        assertThat(wordAdditionGoalReportService.getDictionaryAccessValidator()).isEqualTo(dictionaryAccessValidator);
    }

    private void setupCreateFromScratch() {
        DailyWordAdditionGoalReportRowService dailyRowService = mock(DailyWordAdditionGoalReportRowService.class);
        DailyWordAdditionGoalReportRow dailyRow = mock(DailyWordAdditionGoalReportRow.class);
        when(dailyRowService.buildRowFromScratch(any())).thenReturn(dailyRow);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyWordAdditionGoalReportRowService weeklyRowService = mock(WeeklyWordAdditionGoalReportRowService.class);
        WeeklyWordAdditionGoalReportRow weeklyRow = mock(WeeklyWordAdditionGoalReportRow.class);
        when(weeklyRowService.buildRowFromScratch(any())).thenReturn(weeklyRow);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyWordAdditionGoalReportRowService monthlyRowService = mock(MonthlyWordAdditionGoalReportRowService.class);
        MonthlyWordAdditionGoalReportRow monthlyRow = mock(MonthlyWordAdditionGoalReportRow.class);
        when(monthlyRowService.buildRowFromScratch(any())).thenReturn(monthlyRow);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyWordAdditionGoalReportRowService yearlyRowService = mock(YearlyWordAdditionGoalReportRowService.class);
        YearlyWordAdditionGoalReportRow yearlyRow = mock(YearlyWordAdditionGoalReportRow.class);
        when(yearlyRowService.buildRowFromScratch(any())).thenReturn(yearlyRow);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalWordAdditionGoalReportRowService totalRowService = mock(TotalWordAdditionGoalReportRowService.class);
        TotalWordAdditionGoalReportRow totalRow = mock(TotalWordAdditionGoalReportRow.class);
        when(totalRowService.buildRowFromScratch(any())).thenReturn(totalRow);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        List<WordAdditionGoalReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupUpdateExistingReport() {
        DailyWordAdditionGoalReportRowService dailyRowService = mock(DailyWordAdditionGoalReportRowService.class);
        DailyWordAdditionGoalReportRow dailyRow = mock(DailyWordAdditionGoalReportRow.class);
        when(dailyRowService.updateRow(any(), any())).thenReturn(dailyRow);

        WeeklyWordAdditionGoalReportRowService weeklyRowService = mock(WeeklyWordAdditionGoalReportRowService.class);
        WeeklyWordAdditionGoalReportRow weeklyRow = mock(WeeklyWordAdditionGoalReportRow.class);
        when(weeklyRowService.updateRow(any(), any())).thenReturn(weeklyRow);

        MonthlyWordAdditionGoalReportRowService monthlyRowService = mock(MonthlyWordAdditionGoalReportRowService.class);
        MonthlyWordAdditionGoalReportRow monthlyRow = mock(MonthlyWordAdditionGoalReportRow.class);
        when(monthlyRowService.updateRow(any(), any())).thenReturn(monthlyRow);

        YearlyWordAdditionGoalReportRowService yearlyRowService = mock(YearlyWordAdditionGoalReportRowService.class);
        YearlyWordAdditionGoalReportRow yearlyRow = mock(YearlyWordAdditionGoalReportRow.class);
        when(yearlyRowService.updateRow(any(), any())).thenReturn(yearlyRow);

        TotalWordAdditionGoalReportRowService totalRowService = mock(TotalWordAdditionGoalReportRowService.class);
        TotalWordAdditionGoalReportRow totalRow = mock(TotalWordAdditionGoalReportRow.class);
        when(totalRowService.updateRow(any(), any())).thenReturn(totalRow);

        List<WordAdditionGoalReportRowService> rowServices =
                List.of(dailyRowService, weeklyRowService, monthlyRowService, yearlyRowService, totalRowService);
        setupRowServices(rowServices);
    }

    private void setupRowServices(List<WordAdditionGoalReportRowService> rowServices) {
        ReflectionTestUtils.setField(wordAdditionGoalReportService, "rowServices", rowServices);
    }

    private Set<WordAdditionGoalReportRow> buildRows() {
        DailyWordAdditionGoalReportRow dailyRow = mock(DailyWordAdditionGoalReportRow.class);
        when(dailyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.DAY);

        WeeklyWordAdditionGoalReportRow weeklyRow = mock(WeeklyWordAdditionGoalReportRow.class);
        when(weeklyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.WEEK);

        MonthlyWordAdditionGoalReportRow monthlyRow = mock(MonthlyWordAdditionGoalReportRow.class);
        when(monthlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.MONTH);

        YearlyWordAdditionGoalReportRow yearlyRow = mock(YearlyWordAdditionGoalReportRow.class);
        when(yearlyRow.getRowPeriod()).thenReturn(ReportPeriodUtil.YEAR);

        TotalWordAdditionGoalReportRow totalRow = mock(TotalWordAdditionGoalReportRow.class);
        when(totalRow.getRowPeriod()).thenReturn(ReportPeriodUtil.TOTAL);

        return Set.of(dailyRow, weeklyRow, monthlyRow, yearlyRow, totalRow);
    }

    private TemporaryWordAdditionData buildTemporaryWordAdditionData() {
        return TemporaryWordAdditionData.builder()
                .id(KEY)
                .userId(USER_ID)
                .dictionaryId(10L)
                .addedWords(5)
                .date(LocalDate.of(2024, 7, 12))
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
