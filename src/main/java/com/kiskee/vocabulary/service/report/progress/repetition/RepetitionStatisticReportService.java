package com.kiskee.vocabulary.service.report.progress.repetition;

import com.kiskee.vocabulary.model.dto.report.RepetitionResultData;
import com.kiskee.vocabulary.model.dto.report.UpdateReportResult;
import com.kiskee.vocabulary.model.dto.report.progress.repetition.RepetitionStatisticData;
import com.kiskee.vocabulary.model.entity.report.Report;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReport;
import com.kiskee.vocabulary.model.entity.report.progress.repetition.RepetitionStatisticReportRow;
import com.kiskee.vocabulary.repository.report.RepetitionStatisticReportRepository;
import com.kiskee.vocabulary.service.report.AbstractUpdateReportService;
import com.kiskee.vocabulary.service.report.progress.repetition.row.RepetitionStatisticReportRowService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
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
public class RepetitionStatisticReportService
        extends AbstractUpdateReportService<
                RepetitionStatisticData, RepetitionStatisticReport, RepetitionStatisticReportRow>
        implements RepetitionProgressUpdateReportService {

    private final RepetitionStatisticReportRepository repository;
    private final UserProfileInfoProvider userProfileInfoProvider;

    private final List<RepetitionStatisticReportRowService> rowServices;

    @Async
    @Override
    @Retryable
    @Transactional
    public CompletableFuture<UpdateReportResult> updateReport(RepetitionResultData repetitionResultData) {
        UUID userId = repetitionResultData.getUserId();
        RepetitionStatisticData repetitionStatisticData = buildReportData(userId, repetitionResultData);

        super.updateReport(userId, repetitionStatisticData);
        return CompletableFuture.completedFuture(new UpdateReportResult(Boolean.TRUE));
    }

    @Override
    protected <D> RepetitionStatisticData buildReportData(UUID userId, D data) {
        RepetitionResultData repetitionResultData = (RepetitionResultData) data;
        LocalDate userCreatedAt = userProfileInfoProvider
                .getCreatedAtField(userId)
                .atZone(repetitionResultData.getUserTimeZone())
                .toLocalDate();
        LocalDate currentDate = repetitionResultData
                .getEndTime()
                .atZone(repetitionResultData.getUserTimeZone())
                .toLocalDate();

        return new RepetitionStatisticData(
                userId,
                repetitionResultData.getDictionaryId(),
                userCreatedAt,
                currentDate,
                repetitionResultData.getRightAnswersCount(),
                repetitionResultData.getWrongAnswersCount(),
                repetitionResultData.getSkippedWordsCount(),
                repetitionResultData.getTotalElements(),
                repetitionResultData.getTotalElementsPassed());
    }

    @Override
    protected Optional<RepetitionStatisticReport> getReport(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    protected RepetitionStatisticReport buildReport(UUID userId, Set<RepetitionStatisticReportRow> reportRows) {
        return new RepetitionStatisticReport(userId, reportRows);
    }

    @Override
    protected void saveReport(Report<RepetitionStatisticReportRow> report) {
        repository.save((RepetitionStatisticReport) report);
    }
}
