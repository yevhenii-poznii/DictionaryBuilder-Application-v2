package com.kiskee.vocabulary.model.dto.report.goal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public record WordAdditionData(
        UUID userId, Long dictionaryId, int newWordsPerDayGoal, LocalDate userCreatedAt, LocalDate currentDate, ZoneId userTimeZone) {}