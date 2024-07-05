package com.kiskee.vocabulary.model.dto.report;

import com.kiskee.vocabulary.model.dto.redis.Pause;
import com.kiskee.vocabulary.model.dto.redis.RepetitionData;
import java.time.Instant;
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
    private Instant startTime;
    private Instant endTime;
    private List<Pause> pauses;
    private int rightAnswersCount;
    private int wrongAnswersCount;
    private int skippedWordsCount;
    private int totalElements;
    private int totalElementsPassed;

    public RepetitionResultDataDto(RepetitionData repetitionData) {
        this.userId = repetitionData.getUserId();
        this.dictionaryId = repetitionData.getDictionaryId();
        this.startTime = repetitionData.getStartTime();
        this.endTime = repetitionData.getEndTime();
        this.pauses = repetitionData.getPauses();
        this.rightAnswersCount = repetitionData.getRightAnswersCount();
        this.wrongAnswersCount = repetitionData.getWrongAnswersCount();
        this.skippedWordsCount = repetitionData.getSkippedWordsCount();
        this.totalElements = repetitionData.getTotalElements();
        this.totalElementsPassed = repetitionData.getTotalElementsPassed();
    }

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
