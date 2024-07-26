package com.kiskee.vocabulary.service.report.progress.repetition.time;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import com.kiskee.vocabulary.model.dto.report.goal.RepetitionTimeSpendData;
import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import com.kiskee.vocabulary.model.entity.report.Report;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReport;
import com.kiskee.vocabulary.model.entity.report.goal.time.RepetitionTimeSpendGoalReportRow;
import com.kiskee.vocabulary.repository.report.RepetitionTimeSpendGoalReportRepository;
import com.kiskee.vocabulary.service.report.AbstractUpdateReportService;
import com.kiskee.vocabulary.service.report.progress.repetition.RepetitionProgressUpdateReportService;
import com.kiskee.vocabulary.service.report.progress.repetition.time.row.RepetitionTimeSpendGoalReportRowService;
import com.kiskee.vocabulary.service.user.preference.WordPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
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
        implements RepetitionProgressUpdateReportService {

    private final RepetitionTimeSpendGoalReportRepository repository;
    private final UserProfileInfoProvider userProfileInfoProvider;
    private final WordPreferenceService wordPreferenceService;

    private final List<RepetitionTimeSpendGoalReportRowService> rowServices;

    @Async
    @Override
    @Retryable
    @Transactional
    public CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData) {
        UUID userId = repetitionResultData.getUserId();
        RepetitionTimeSpendData repetitionTimeSpendData = buildReportData(userId, repetitionResultData);

        super.updateReport(userId, repetitionTimeSpendData);
        return CompletableFuture.completedFuture(new UpdateReportResult(Boolean.TRUE));
    }

    @Override
    protected Optional<RepetitionTimeSpendGoalReport> getReport(UUID userId) {
        return repository.findByUserId(userId);
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
