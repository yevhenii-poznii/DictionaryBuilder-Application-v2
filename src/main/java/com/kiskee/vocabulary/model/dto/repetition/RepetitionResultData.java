package com.kiskee.vocabulary.model.dto.repetition;

import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface RepetitionResultData {

    UUID getUserId();

    long getDictionaryId();

    ZoneId getUserTimeZone();

    Instant getStartTime();

    Instant getEndTime();

    List<Pause> getPauses();

    int getRightAnswersCount();

    int getWrongAnswersCount();

    int getSkippedWordsCount();

    int getTotalElements();

    int getTotalElementsPassed();
}
