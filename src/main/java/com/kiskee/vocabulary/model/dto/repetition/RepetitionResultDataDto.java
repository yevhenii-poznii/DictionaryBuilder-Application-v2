package com.kiskee.vocabulary.model.dto.repetition;

import com.kiskee.vocabulary.model.entity.redis.repetition.Pause;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepetitionResultDataDto implements RepetitionResultData {

    private UUID userId;
    private long dictionaryId;
    private String dictionaryName;
    private ZoneId userTimeZone;
    private Instant startTime;
    private Instant endTime;
    private List<Pause> pauses = new ArrayList<>();
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;

    protected void incrementRightAnswersCount() {
        this.rightAnswersCount++;
    }

    protected void incrementWrongAnswersCount() {
        this.wrongAnswersCount++;
    }

    protected void incrementSkippedWordsCount() {
        this.skippedWordsCount++;
    }

    protected void incrementTotalElementsPassed() {
        this.totalElementsPassed++;
    }
}
