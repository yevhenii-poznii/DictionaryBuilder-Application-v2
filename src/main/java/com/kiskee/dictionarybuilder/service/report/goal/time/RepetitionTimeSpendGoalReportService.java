package com.kiskee.dictionarybuilder.service.report.goal.time;

import com.kiskee.dictionarybuilder.mapper.report.RepetitionTimeSpendGoalReportMapper;
import com.kiskee.dictionarybuilder.model.dto.repetition.RepetitionResultData;
import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.update.UpdateReportResult;
import com.kiskee.dictionarybuilder.model.dto.report.update.goal.RepetitionTimeSpendData;
import com.kiskee.dictionarybuilder.model.entity.redis.repetition.Pause;
import com.kiskee.dictionarybuilder.model.entity.report.Report;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.dictionarybuilder.repository.report.RepetitionTimeSpendGoalReportRepository;
import com.kiskee.dictionarybuilder.service.report.AbstractUpdateReportService;
import com.kiskee.dictionarybuilder.service.report.ReportService;
import com.kiskee.dictionarybuilder.service.report.goal.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.dictionarybuilder.service.report.progress.repetition.RepetitionProgressUpdateReportService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.user.preference.WordPreferenceService;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileInfoProvider;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class RepetitionTimeSpendGoalReportService
        extends AbstractUpdateReportService<
                RepetitionTimeSpendData, RepetitionTimeSpendGoalReport, RepetitionTimeSpendGoalReportRow>
        implements ReportService, RepetitionProgressUpdateReportService {

    private final RepetitionTimeSpendGoalReportRepository repository;
    private final RepetitionTimeSpendGoalReportMapper mapper;
    private final UserProfileInfoProvider userProfileInfoProvider;
    private final WordPreferenceService wordPreferenceService;
    private final CurrentDateTimeService currentDateTimeService;

    private final List<RepetitionTimeSpendGoalReportRowService> rowServices;

    @Override
    public ReportDto getReport() {
        return super.getReport();
    }

    @Async
    @Override
    @Transactional
    @Retryable(
            recover = "recoverUpdateReport",
            maxAttemptsExpression = "${retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${retry.delay}", multiplier = 2.0))
    public CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData) {
        try {
            UUID userId = repetitionResultData.getUserId();
            RepetitionTimeSpendData repetitionTimeSpendData = buildReportData(userId, repetitionResultData);

            super.updateReport(userId, repetitionTimeSpendData);
            return CompletableFuture.completedFuture(new UpdateReportResult(Boolean.TRUE));
        } catch (Exception e) {
            log.error(
                    "Failed to update repetition time spend goal report for user: {}",
                    repetitionResultData.getUserId());
            throw e;
        }
    }

    @Override
    protected Optional<RepetitionTimeSpendGoalReport> getReport(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    protected ReportDto mapToDto(UUID userId, Set<RepetitionTimeSpendGoalReportRow> reportRows) {
        return mapper.toDto(new RepetitionTimeSpendGoalReport(userId, reportRows));
    }

    @Override
    protected <D> RepetitionTimeSpendData buildReportData(UUID userId, D data) {
        RepetitionResultData repetitionResultData = (RepetitionResultData) data;
        LocalDate userCreatedAt = userProfileInfoProvider
                .getCreatedAtField(userId)
                .atZone(repetitionResultData.getUserTimeZone())
                .toLocalDate();
        Duration repetitionDurationGoal =
                wordPreferenceService.getWordPreference(userId).dailyRepetitionDurationGoal();

        Duration currentRepetitionDuration = calculateCurrentRepetitionDuration(
                repetitionResultData.getStartTime(),
                repetitionResultData.getEndTime(),
                repetitionResultData.getPauses());
        LocalDate currentDate = repetitionResultData
                .getEndTime()
                .atZone(repetitionResultData.getUserTimeZone())
                .toLocalDate();

        return new RepetitionTimeSpendData(
                userId,
                repetitionResultData.getDictionaryId(),
                repetitionResultData.getDictionaryName(),
                currentRepetitionDuration,
                repetitionDurationGoal,
                userCreatedAt,
                currentDate);
    }

    @Override
    protected RepetitionTimeSpendGoalReport buildReport(UUID userId, Set<RepetitionTimeSpendGoalReportRow> reportRows) {
        return new RepetitionTimeSpendGoalReport(userId, reportRows);
    }

    @Override
    protected void saveReport(Report<RepetitionTimeSpendGoalReportRow> report) {
        repository.save((RepetitionTimeSpendGoalReport) report);
    }

    private Duration calculateCurrentRepetitionDuration(Instant startTime, Instant endTime, List<Pause> pauses) {
        Duration pausesDuration = pauses.stream()
                .reduce(
                        Duration.ZERO,
                        (duration, pause) -> duration.plus(Duration.between(pause.getStartTime(), pause.getEndTime())),
                        Duration::plus);

        return Duration.between(startTime, endTime).minus(pausesDuration);
    }
}
