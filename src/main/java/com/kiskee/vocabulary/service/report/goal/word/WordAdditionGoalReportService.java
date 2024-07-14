package com.kiskee.vocabulary.service.report.goal.word;

import com.kiskee.vocabulary.model.dto.report.goal.WordAdditionData;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReport;
import com.kiskee.vocabulary.model.entity.report.word.WordAdditionGoalReportRow;
import com.kiskee.vocabulary.repository.report.WordAdditionGoalReportRepository;
import com.kiskee.vocabulary.service.report.UpdateGoalReportService;
import com.kiskee.vocabulary.service.report.goal.word.row.WordAdditionGoalReportRowService;
import com.kiskee.vocabulary.service.user.profile.UserProfileInfoProvider;
import com.kiskee.vocabulary.util.TimeZoneContextHolder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordAdditionGoalReportService implements UpdateGoalReportService {

    private final WordAdditionGoalReportRepository repository;
    private final UserProfileInfoProvider userProfileInfoProvider;

    private final List<WordAdditionGoalReportRowService> rowServices;

    @Override
    public void updateReport(UUID userId, Long dictionaryId, int addedWords, int newWordsPerDayGoal) {
        Optional<WordAdditionGoalReport> report = repository.findByUserId(userId);

        ZoneId userTimeZone = TimeZoneContextHolder.getTimeZone();
        LocalDate userCreatedAt = userProfileInfoProvider
                .getCreatedAtField(userId)
                .atZone(userTimeZone)
                .toLocalDate();
        LocalDate currentDateAtUserTimeZone = LocalDate.now(userTimeZone);

        WordAdditionData wordAdditionData = new WordAdditionData(
                userId,
                dictionaryId,
                addedWords,
                newWordsPerDayGoal,
                userCreatedAt,
                currentDateAtUserTimeZone,
                userTimeZone);

        if (report.isPresent()) {
            updateExistingReport(wordAdditionData, report.get());
            return;
        }
        createReportFromScratch(wordAdditionData);
    }

    private void createReportFromScratch(WordAdditionData wordAdditionData) {
        Set<WordAdditionGoalReportRow> rows = buildRows(wordAdditionData);
        WordAdditionGoalReport report = new WordAdditionGoalReport(wordAdditionData.userId(), rows);
        repository.save(report);

        log.info(
                "{} report created from scratch for user: {}",
                report.getClass().getSimpleName(),
                wordAdditionData.userId());
    }

    private void updateExistingReport(WordAdditionData wordAdditionData, WordAdditionGoalReport report) {
        Set<WordAdditionGoalReportRow> updatedRows = updateRows(wordAdditionData, report.getReportRows());

        WordAdditionGoalReport updatedReport = report.buildFrom(updatedRows);
        repository.save(updatedReport);

        log.info(
                "{} report updated for user: {}",
                WordAdditionGoalReport.class.getSimpleName(),
                wordAdditionData.userId());
    }

    private Set<WordAdditionGoalReportRow> buildRows(WordAdditionData wordAdditionData) {
        return rowServices.stream()
                .map(rowService -> rowService.buildRowFromScratch(wordAdditionData))
                .collect(Collectors.toSet());
    }

    private Set<WordAdditionGoalReportRow> updateRows(
            WordAdditionData wordAdditionData, Set<WordAdditionGoalReportRow> rows) {
        Map<String, WordAdditionGoalReportRow> rowsMap = toRowsMap(rows);

        return rowServices.stream()
                .map(rowService -> rowService.updateRow(rowsMap.get(rowService.getRowPeriod()), wordAdditionData))
                .collect(Collectors.toSet());
    }

    private Map<String, WordAdditionGoalReportRow> toRowsMap(Set<WordAdditionGoalReportRow> rows) {
        return rows.stream().collect(Collectors.toMap(WordAdditionGoalReportRow::getRowPeriod, Function.identity()));
    }
}
