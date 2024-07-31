package com.kiskee.dictionarybuilder.model.dto.repetition;

import com.kiskee.dictionarybuilder.model.entity.redis.repetition.Pause;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public interface RepetitionResultData {

    UUID getUserId();

    long getDictionaryId();

    String getDictionaryName();

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
