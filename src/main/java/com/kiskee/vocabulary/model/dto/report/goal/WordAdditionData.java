package com.kiskee.vocabulary.model.dto.report.goal;

import java.time.LocalDate;
import java.util.UUID;

public record WordAdditionData(
        UUID userId,
        Long dictionaryId,
        int addedWords,
        int newWordsPerDayGoal,
        LocalDate userCreatedAt,
        LocalDate currentDate) {}
