package com.kiskee.vocabulary.service.report;

import java.util.UUID;

public interface UpdateGoalReportService {

    void updateReport(UUID userId, Long dictionaryId, int newWordsPerDayGoal);
}
