package com.kiskee.dictionarybuilder.service.report.goal.word;

import com.kiskee.dictionarybuilder.mapper.report.WordAdditionGoalReportMapper;
import com.kiskee.dictionarybuilder.model.dto.report.ReportDto;
import com.kiskee.dictionarybuilder.model.dto.report.update.goal.WordAdditionData;
import com.kiskee.dictionarybuilder.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.dictionarybuilder.model.entity.report.Report;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReport;
import com.kiskee.dictionarybuilder.model.entity.report.goal.word.WordAdditionGoalReportRow;
import com.kiskee.dictionarybuilder.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.dictionarybuilder.repository.report.WordAdditionGoalReportRepository;
import com.kiskee.dictionarybuilder.service.report.AbstractUpdateReportService;
import com.kiskee.dictionarybuilder.service.report.ReportService;
import com.kiskee.dictionarybuilder.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.dictionarybuilder.service.time.CurrentDateTimeService;
import com.kiskee.dictionarybuilder.service.user.preference.WordPreferenceService;
import com.kiskee.dictionarybuilder.service.user.profile.UserProfileInfoProvider;
import com.kiskee.dictionarybuilder.service.vocabulary.dictionary.DictionaryAccessValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class WordAdditionGoalReportService
        extends AbstractUpdateReportService<WordAdditionData, WordAdditionGoalReport, WordAdditionGoalReportRow>
        implements ReportService, UpdateGoalReportService {

    private final TemporaryWordAdditionCacheRepository temporaryWordAdditionCacheRepository;
    private final WordAdditionGoalReportRepository repository;
    private final WordAdditionGoalReportMapper mapper;
    private final UserProfileInfoProvider userProfileInfoProvider;
    private final WordPreferenceService wordPreferenceService;
    private final DictionaryAccessValidator dictionaryAccessValidator;
    private final CurrentDateTimeService currentDateTimeService;

    private final List<WordAdditionGoalReportRowService> rowServices;

    @Override
    public ReportDto getReport() {
        return super.getReport();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Retryable(
            maxAttemptsExpression = "${retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${retry.delay}", multiplier = 2.0))
    public void updateReport(String temporaryWordAdditionDataKey) {
        try {
            Optional<TemporaryWordAdditionData> temporaryWordAdditionDataOptional =
                    temporaryWordAdditionCacheRepository.findById(temporaryWordAdditionDataKey);
            if (temporaryWordAdditionDataOptional.isEmpty()) {
                log.warn("Temporary word addition data not found for key: {}", temporaryWordAdditionDataKey);
                return;
            }
            TemporaryWordAdditionData temporaryWordAdditionData = temporaryWordAdditionDataOptional.get();
            UUID userId = temporaryWordAdditionData.getUserId();
            WordAdditionData wordAdditionData = buildReportData(userId, temporaryWordAdditionData);

            super.updateReport(userId, wordAdditionData);
        } catch (Exception e) {
            log.error("Failed to update word addition goal report for user: {}", temporaryWordAdditionDataKey);
            throw e;
        }
    }

    @Override
    protected <D> WordAdditionData buildReportData(UUID userId, D data) {
        TemporaryWordAdditionData temporaryWordAdditionData = (TemporaryWordAdditionData) data;
        LocalDate userCreatedAt = userProfileInfoProvider
                .getCreatedAtField(userId)
                .atZone(temporaryWordAdditionData.getUserTimeZone())
                .toLocalDate();
        int newWordsPerDayGoal = wordPreferenceService.getWordPreference(userId).newWordsPerDayGoal();
        String dictionaryName = dictionaryAccessValidator
                .getDictionaryByIdAndUserId(temporaryWordAdditionData.getDictionaryId(), userId)
                .getDictionaryName();

        return new WordAdditionData(
                userId,
                temporaryWordAdditionData.getDictionaryId(),
                dictionaryName,
                temporaryWordAdditionData.getAddedWords(),
                newWordsPerDayGoal,
                userCreatedAt,
                temporaryWordAdditionData.getDate());
    }

    @Override
    protected Optional<WordAdditionGoalReport> getReport(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    protected ReportDto mapToDto(UUID userId, Set<WordAdditionGoalReportRow> reportRows) {
        return mapper.toDto(new WordAdditionGoalReport(userId, reportRows));
    }

    @Override
    protected WordAdditionGoalReport buildReport(UUID userId, Set<WordAdditionGoalReportRow> reportRows) {
        return new WordAdditionGoalReport(userId, reportRows);
    }

    @Override
    protected void saveReport(Report<WordAdditionGoalReportRow> report) {
        repository.save((WordAdditionGoalReport) report);
    }
}
