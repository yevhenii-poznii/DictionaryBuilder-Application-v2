package com.kiskee.vocabulary.model.dto.report;

import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface RepetitionResultData {

    UUID getUserId();

    long getDictionaryId();

    Instant getStartTime();

    Instant getEndTime();

    List<Pause> getPauses();

    int getRightAnswersCount();

    int getWrongAnswersCount();

    int getSkippedWordsCount();

    int getTotalElements();

    int getTotalElementsPassed();
}
