package com.kiskee.vocabulary.model.dto.report.goal;

import java.time.Instant;
import java.util.UUID;

public record WordAdditionData(UUID userId, Long dictionaryId, int newWordsPerDayGoal, Instant userCreatedAt) {
}
