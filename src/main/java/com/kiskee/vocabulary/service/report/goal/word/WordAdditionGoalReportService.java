package com.kiskee.vocabulary.service.report.goal.word;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.redis.TemporaryWordAdditionData;
import com.kiskee.vocabulary.model.entity.report.Report;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.repository.redis.TemporaryWordAdditionCacheRepository;
import com.kiskee.vocabulary.repository.report.WordAdditionGoalReportRepository;
import com.kiskee.vocabulary.service.report.AbstractUpdateReportService;
import com.kiskee.vocabulary.service.report.UpdateGoalReportService;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.user.preference.WordPreferenceService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        implements UpdateGoalReportService {

    private final TemporaryWordAdditionCacheRepository temporaryWordAdditionCacheRepository;
    private final WordAdditionGoalReportRepository repository;
    private final UserProfileInfoProvider userProfileInfoProvider;
    private final WordPreferenceService wordPreferenceService;

    private final List<WordAdditionGoalReportRowService> rowServices;

    @Override
    @Retryable
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateReport(String temporaryWordAdditionDataKey) {
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
    }

    @Override
    protected <D> WordAdditionData buildReportData(UUID userId, D data) {
        TemporaryWordAdditionData temporaryWordAdditionData = (TemporaryWordAdditionData) data;
        LocalDate userCreatedAt = userProfileInfoProvider
                .getCreatedAtField(userId)
                .atZone(temporaryWordAdditionData.getUserTimeZone())
                .toLocalDate();
        int newWordsPerDayGoal = wordPreferenceService.getWordPreference(userId).newWordsPerDayGoal();

        return new WordAdditionData(
                userId,
                temporaryWordAdditionData.getDictionaryId(),
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
    protected WordAdditionGoalReport buildReport(UUID userId, Set<WordAdditionGoalReportRow> reportRows) {
        return new WordAdditionGoalReport(userId, reportRows);
    }

    @Override
    protected void saveReport(Report<WordAdditionGoalReportRow> report) {
        repository.save((WordAdditionGoalReport) report);
    }
}
