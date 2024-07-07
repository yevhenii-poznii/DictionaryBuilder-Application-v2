package com.kiskee.vocabulary.service.report.goal.word;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.repository.report.WordAdditionGoalReportRepository;
import com.kiskee.vocabulary.service.report.UpdateGoalReportService;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordAdditionGoalReportService implements UpdateGoalReportService {

    private final WordAdditionGoalReportRepository repository;
    private final UserProfileInfoProvider userProfileInfoProvider;

    private final List<WordAdditionGoalReportRowService> rowServices;

    @Override
    public void updateReport(UUID userId, Long dictionaryId, int newWordsPerDayGoal) {
        Optional<WordAdditionGoalReport> report = repository.findByUserId(userId);
        Instant userCreatedAt = userProfileInfoProvider.getCreatedAtField(userId);

        WordAdditionData wordAdditionData =
                new WordAdditionData(userId, dictionaryId, newWordsPerDayGoal, userCreatedAt);

        if (report.isPresent()) {
            updateExistingReport(wordAdditionData);
            return;
        }
        createReportFromScratch(wordAdditionData);
    }

    private void createReportFromScratch(WordAdditionData wordAdditionData) {
        Set<WordAdditionGoalReportRow> rows = rowServices.stream()
                .map(rowService -> rowService.buildRowFromScratch(wordAdditionData))
                .collect(Collectors.toSet());

        WordAdditionGoalReport report = new WordAdditionGoalReport(wordAdditionData.userId(), rows);
        repository.save(report);
    }

    private void updateExistingReport(WordAdditionData wordAdditionData) {
//        rowServices.getFirst();
//        Set<Object> rows = rowServices.stream()
//                .filter(rowService -> !(rowService instanceof DailyWordAdditionGoalReportRow))
//                .map(rowService -> rowService.reCalculate(wordAdditionData))
//                .collect(Collectors.toSet());

        WordAdditionGoalReport report = null;
        repository.save(report);
    }
}
